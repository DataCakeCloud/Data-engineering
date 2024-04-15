package com.ushareit.dstask.service.impl;


import com.ushareit.dstask.bean.JDBCLock;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.LockMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.LockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class LockServiceImpl extends AbstractBaseServiceImpl<JDBCLock> implements LockService {

    private static final Integer DEFAULT_EXPIRED_SECONDS = 10;

    @Resource
    private LockMapper lockMapper;

    @Override
    public CrudMapper<JDBCLock> getBaseMapper() {
        return lockMapper;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean tryLock(String tag, Integer expiredSeconds) {
        if (StringUtils.isEmpty(tag)) {
            throw new NullPointerException();
        }

        Example example = new Example(JDBCLock.class);
        example.or()
                .andEqualTo("tag", tag)
                .andEqualTo("status", JDBCLock.LOCKED_STATUS);
        List<JDBCLock> lockList = lockMapper.selectByExample(example);

        if (lockList.size() > NumberUtils.INTEGER_ONE) {
            lockMapper.deleteByTag(tag);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR, String.format("多个主机锁定 %s", tag));
        }

        if (CollectionUtils.isEmpty(lockList)) {
            try {
                super.save(new JDBCLock(tag,
                        CommonConstant.HOSTNAME,
                        this.addSeconds(new Date(), expiredSeconds),
                        JDBCLock.LOCKED_STATUS,
                        new Timestamp(System.currentTimeMillis()),
                        new Timestamp(System.currentTimeMillis())));
            } catch (Exception e) {
                throw new ServiceException(BaseResponseCodeEnum.CLI_SAVE_DB_FAIL, "此标记锁已经存在");
            }
            return true;
        }

        // 如果锁已经
        JDBCLock lock = lockList.get(NumberUtils.INTEGER_ZERO);
        //Date expiredTime = lock.getExpirationTime();
        Date now = new Date();
        //如果失效时间在现在的时间之前，更新了它的失效时间
        if (lock.getExpirationTime().before(now)) {
            JDBCLock toUpdateParam = new JDBCLock();
            toUpdateParam.setExpirationTime(this.addSeconds(now, expiredSeconds));
            toUpdateParam.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            toUpdateParam.setHostname(CommonConstant.HOSTNAME);

            Example lockExample = new Example(JDBCLock.class);
            lockExample.or()
                    .andEqualTo("id", lock.getId())
                    .andEqualTo("expirationTime", lock.getExpirationTime());
            int result = lockMapper.updateByExampleSelective(toUpdateParam, lockExample);
            log.debug("lock result for tag {} is {}", tag, result);
            // 如果返回0，说明没有锁定成功
            return BooleanUtils.toBoolean(result);
        }

        // 如果是当前机器在持有锁，则返回 true
        return StringUtils.equalsIgnoreCase(lock.getHostname(), CommonConstant.HOSTNAME);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void unlock(String tag) {
        lockMapper.deleteByTag(tag);
    }

    private Timestamp addSeconds(Date date, Integer seconds) {
        if (Objects.isNull(seconds)) {
            seconds = DEFAULT_EXPIRED_SECONDS;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return new Timestamp(calendar.getTime().getTime());
    }
}
