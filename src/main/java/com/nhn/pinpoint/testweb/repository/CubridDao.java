package com.nhn.pinpoint.testweb.repository;

/**
 *
 */
public interface CubridDao {
    int selectOne();

    void createStatement();
    
    void createErrorStatement();
}
