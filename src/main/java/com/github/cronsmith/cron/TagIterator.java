package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * 
 * @Description: TagIterator
 * @Author: Fred Feng
 * @Date: 13/03/2025
 * @Version 1.0.0
 */
public interface TagIterator extends Iterator<LocalDateTime>, Serializable {

    String getTag();

    void reset();

}
