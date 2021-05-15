package com.pefgloble.pefchate.util;



import com.pefgloble.pefchate.helpers.ListenableFuture;

import java.util.concurrent.ExecutionException;

public abstract class AssertedSuccessListener<T> implements ListenableFuture.Listener<T> {
  @Override
  public void onFailure(ExecutionException e) {
    throw new AssertionError(e);
  }
}
