package com.huitong.app.dao.mapper;

import com.huitong.app.model.DisLockInfo;
import org.springframework.stereotype.Repository;

/**
 * author pczhao
 * date  2019-12-31 17:54
 */

@Repository
public interface LockInfoMapper {

    long queryRecordCount();

    int addLockInfo(DisLockInfo disLockInfo);

}
