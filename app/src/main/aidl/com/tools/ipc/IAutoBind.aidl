// IAutoBind.aidl
package com.tools.ipc;

// Declare any non-default types here with import statements

interface IAutoBind {
    void onBindService(IBinder aConnect);
}
