package com.huitong.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author pczhao
 * date  2019-12-31 17:52
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisLockInfo {
    private Long id;
    private long number;
    private String ip;
}
