package org.spr.methodAnalysis;

import java.util.List;

public interface DataSender {
    boolean sendData(Object data) throws Exception;

    boolean sendData(List<? extends Object> data) throws Exception;
}