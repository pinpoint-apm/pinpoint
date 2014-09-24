package com.nhn.pinpoint.testweb.repository;

/**
 *
 */
public interface CubridDao {
    int selectOne();

    boolean createStatement();
    
    boolean createErrorStatement();
}
