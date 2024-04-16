package com.ushareit.dstask.web.utils.excel;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.UUID;


/**
 * 文件输出到客户端的工具类
 * @author caoxuedong
 */
@Slf4j
public class FileRenderUtil {


	/**
	 * 下载文件
	 * @param stream
	 * @param length
	 * @param fileName
	 */
	public static void download(InputStream stream, Long length, String fileName, HttpServletResponse response) {
		//HttpServletResponse response = SpringMVCUtil.getResponse();
		response.setContentType("text/html;charset=utf-8");   
        response.setCharacterEncoding("UTF-8");   
        BufferedInputStream bis = null;   
        BufferedOutputStream bos = null;
        try {
            response.setContentType("application/x-msdownload;");
            response.setHeader("Content-disposition", "attachment; filename="+ new String(fileName.getBytes("utf-8"), "ISO8859-1"));
            response.setHeader("Content-Length", String.valueOf(length));
            bis = new BufferedInputStream(stream);
            bos = new BufferedOutputStream(response.getOutputStream());   
            byte[] buff = new byte[2048];   
            int bytesRead;   
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {   
                bos.write(buff, 0, bytesRead);   
            }   
        } catch (Exception e) {   
            e.printStackTrace();   
        } finally {   
            if (bis != null){
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
            if (bos != null){
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }
	}


	/**
	 * 向客户端输出浏览内容
	 * @param stream
	 * @param length
	 * @param contentType 图片 image/jpeg, 视频 audio/mpeg, 应用程序 application/octet-stream
	 */
	public static void show(InputStream stream, Long length, String contentType, HttpServletResponse response) {
		OutputStream toClient = null;
		InputStream fis = null;
		byte[] buffer = null;
		//HttpServletResponse response = SpringMVCUtil.getResponse();
		try {
			fis = new BufferedInputStream(stream);
			buffer = new byte[1024];
			response.reset();
			response.setContentLength(length.intValue());
			response.setContentType(contentType);
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			toClient = new BufferedOutputStream(response.getOutputStream());
			int bytesRead;
			while (-1 != (bytesRead = fis.read(buffer, 0, buffer.length))) {
				toClient.write(buffer, 0, bytesRead);
				toClient.flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (toClient != null) {
				try {
					toClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * 导出excel
	 * @param sheetList
	 * @param fileName
     */
	public static void renderExcel(List<? extends AbstractExcelSheetVO> sheetList, String fileName, HttpServletRequest request, HttpServletResponse response){
		OutputStream out = null;
		try {
			File dir = new File(getTempDir());
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(dir.getAbsolutePath() + File.separator + UUID.randomUUID().toString());
			out = new FileOutputStream(file);
			ExcelUtil.exportExcel(sheetList, out);
			//out.close();
			download(file, fileName, request, response);
			//download(file, "统计报表.xls", request, response);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("renderExcel error: {}", e);
		}finally {
			try {
				if(out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.error("renderExcel close OutputStream error: {}", e);
			}
		}
	}

	public static void renderExcel(List<? extends AbstractExcelSheetVO> sheetList, String fileName){
		OutputStream out = null;
		try {
			File file = new File(fileName);
			out = new FileOutputStream(file);
			ExcelUtil.exportExcel(sheetList, out);
			out.close();
			//download(file, fileName, request, response);
			//download(file, "统计报表.xls", request, response);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("renderExcel error: {}", e);
		}finally {
			try {
				if(out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.error("renderExcel close OutputStream error: {}", e);
			}
		}
	}


	private static void download(File file, String sourceName, HttpServletRequest request, HttpServletResponse response) {
		OutputStream toClient = null;
		RandomAccessFile raf = null;
		byte[] buffer = null;
		String charsetName = "utf-8";
		if (isIE(request)) {
			charsetName = "gb2312";
		}
		try {
			raf = new RandomAccessFile(file, "r");
			buffer = new byte[1024];
			response.reset();
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
			//use it, if Chrome warning : Resource interpreted as Document but transferred with MIME type application/octet-stream
			//response.setContentType("text/html");

			response.addHeader("Content-Disposition", "attachment;filename=" + new String(sourceName.getBytes(charsetName), "iso8859-1"));
			// tell the client to allow accept-ranges
			response.addHeader("Accept-Ranges", "bytes");

			Long start = 0L;// 包含
			Long fileLength = file.length();
			Long end = fileLength;// 不包含
			// client requests a file block download start byte
			if (request.getHeader("Range") != null) {
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
				String str = request.getHeader("Range").replaceAll("bytes=", "");
				if (str.indexOf("-") != -1) {
					start = Long.parseLong(str.split("-")[0].trim());
					if (str.split("-").length > 1 && StringUtils.isNotBlank(str.split("-")[1])) {
						end = Long.parseLong(str.split("-")[1].trim()) + 1;
					}
				}
			} else {
				response.setStatus(HttpServletResponse.SC_OK);
			}
			if (start > fileLength) {
				start = fileLength;
			}
			if (end > fileLength) {
				end = fileLength;
			}
			// support multi-threaded download
			// respone format:
			// Content-Length:[file size] - [client request start bytes from file block]
			response.setHeader("Content-Length", new Long(end - start).toString());

			if (start != 0) {
				// 断点开始
				// 响应的格式是:
				// Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
				String contentRange = "bytes " + new Long(start).toString() + "-" + new Long(end - 1).toString() + "/" + new Long(fileLength).toString();
				response.setHeader("Content-Range", contentRange);
				raf.seek(start);
			}

			toClient = new BufferedOutputStream(response.getOutputStream());
			int bytesRead;
			if (end - start > buffer.length) {
				int hasRead = 0;
				int needRead = buffer.length;
				while ((bytesRead = raf.read(buffer, 0, needRead)) != -1) {
					toClient.write(buffer, 0, bytesRead);
					hasRead += bytesRead;
					if (end - start == hasRead) {
						break;
					}
					if (hasRead + needRead > end - start) {
						needRead = Long.valueOf(end - start).intValue() - hasRead;
					}
				}
			} else {
				bytesRead = raf.read(buffer, 0, Long.valueOf((end - start)).intValue());
				toClient.write(buffer, 0, bytesRead);
			}
			toClient.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (toClient != null) {
				try {
					toClient.close();
				} catch (IOException e) {
				}
			}
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
				}
			}
		}
	}


	public static String getTempDir() {
		String tempPath = System.getProperty("java.io.tmpdir");
		File tempFile = new File(tempPath);
		if (!tempFile.exists()) {
			tempFile.mkdirs();
		}
		return tempFile.getAbsolutePath();
	}

	private static boolean isIE(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		return userAgent.contains("MSIE");
	}



}
