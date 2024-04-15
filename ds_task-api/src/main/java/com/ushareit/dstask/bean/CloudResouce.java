package com.ushareit.dstask.bean;


import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.web.utils.UrlUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author xuebotao
 * @date 2022/12/21
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CloudResouce extends BaseEntity {

    public Integer total;

    public List<DataResource> list;


    @Data
    public static class DataResource {

        public String provider;
        public String name;
        public Integer tenantId;
        public String region;
        public String regionAlias;
        public String roleName;
        public String storage;
        public String bucket;
        public String path;
        private String providerAlias;

        public DataResource() {

        }

        /**
         * 默认桶
         * @return
         */
        public String getBucket() {
            if (!storage.endsWith("/")) {
                storage = storage + "/";
            }
            Matcher matcher = UrlUtil.getMatcher(storage, DsTaskConstant.OBS_AWS_PATH_PATTERN);
            return matcher.group(2);
        }

        /**
         * 默认路径带这桶名 ，为了规范都给最后加/
         * @return
         */
        public String getStorage() {
            if (!storage.endsWith("/")) {
                storage = storage + "/";
            }
            return storage;
        }

        /**
         * 只取得桶后面的路径
         *
         * @return
         */
        public String getPath() {
            if (!storage.endsWith("/")) {
                storage = storage + "/";
            }
            Matcher matcher = UrlUtil.getMatcher(storage, DsTaskConstant.OBS_AWS_PATH_PATTERN);
            return matcher.group(3);
        }

    }


}
