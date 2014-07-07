package com.bj4.yhh.accountant.service;
import com.bj4.yhh.accountant.service.ParseServiceCallback;
interface ParseServiceBinder {
void registerCallback(ParseServiceCallback cb);
void unregisterCallback(ParseServiceCallback cb);
}