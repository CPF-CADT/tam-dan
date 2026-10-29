package com.tamdan.mobile.runtime

class SynchronizationCoordinator {
    private var state = SyncState.IDLE

    fun markPending(): SyncState { state = SyncState.PENDING; return state }
    fun begin(): SyncState {
        state = if (state == SyncState.OFFLINE) SyncState.OFFLINE else SyncState.SYNCING
        return state
    }
    fun onNetworkLost(): SyncState { state = SyncState.OFFLINE; return state }
    fun onUploadSucceeded(): SyncState { state = SyncState.COMPLETE; return state }
    fun onUploadFailed(): SyncState { state = SyncState.FAILED; return state }
    fun recover(): SyncState { state = SyncState.PENDING; return state }
    fun current(): SyncState = state
}
