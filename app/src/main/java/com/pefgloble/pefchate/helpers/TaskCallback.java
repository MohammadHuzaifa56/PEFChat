package com.pefgloble.pefchate.helpers;

public interface TaskCallback<T> {
  void onTaskDone(T data);
}
