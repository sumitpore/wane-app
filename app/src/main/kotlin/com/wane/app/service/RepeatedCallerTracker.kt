package com.wane.app.service

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepeatedCallerTracker
    @Inject
    constructor() {
        private val calls = ConcurrentHashMap<String, MutableList<Long>>()

        fun recordCall(number: String) {
            val now = System.currentTimeMillis()
            val list = calls.getOrPut(number) { mutableListOf() }
            synchronized(list) {
                pruneList(list, now)
                list.add(now)
            }
            pruneEmptyEntries()
        }

        fun isRepeatedCaller(number: String): Boolean {
            val now = System.currentTimeMillis()
            val list = calls[number] ?: return false
            synchronized(list) {
                pruneList(list, now)
                return list.size >= REPEAT_THRESHOLD
            }
        }

        fun reset() {
            calls.clear()
        }

        private fun pruneList(
            list: MutableList<Long>,
            now: Long,
        ) {
            val cutoff = now - WINDOW_MS
            list.removeAll { it < cutoff }
        }

        private fun pruneEmptyEntries() {
            val iter = calls.entries.iterator()
            while (iter.hasNext()) {
                val e = iter.next()
                synchronized(e.value) {
                    if (e.value.isEmpty()) iter.remove()
                }
            }
        }

        companion object {
            private const val WINDOW_MS = 5 * 60 * 1000L
            private const val REPEAT_THRESHOLD = 3
        }
    }
