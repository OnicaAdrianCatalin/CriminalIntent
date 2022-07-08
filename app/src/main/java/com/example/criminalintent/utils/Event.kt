package com.example.criminalintent.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 *
 * This wrapper makes sure that the event can only be consumed once and can be used to solve any situation
 * when an observer gets an already consumed and invalid LiveData at observation time, e.g.:
 *
 * 1. Fragment sets up a LiveData observer to observe an operation status.
 * 2. User triggers an operation
 * 3. When operation is finished, operation status is sent out by the ViewModel. (ViewModel follows the activity lifecycle).
 * 4. Fragment gets the operation status update and reacts to that, e.g., show a Toast or Dialog.
 * 5. User navigates out of the Fragment.
 * 6. User navigates back to the Fragment.
 * ===> Expected behaviour: Neither Toast/Dialog is shown
 *      Actual behaviour: Toast/Dialog is shown again although the operation was done before
 *
 * Original class was shared by https://gist.github.com/JoseAlcerreca.
 */
data class Event<out Content>(private val content: Content) {
    private var hasBeenHandled = AtomicBoolean(false)

    /**
     * Returns the content and prevents its use again.
     */
    internal fun runIfNotHandled(block: (Content) -> Unit) {
        if (!hasBeenHandled.getAndSet(true)) {
            block(content)
        }
    }
}

fun <T> LiveData<Event<T>>.observeEvent(
    lifecycleOwner: LifecycleOwner,
    observer: (T) -> Unit
) = observe(lifecycleOwner) { event ->
    event.runIfNotHandled { content -> observer(content) }
}

fun MutableLiveData<Event<Unit>>.postEvent() = postValue(Event(Unit))
