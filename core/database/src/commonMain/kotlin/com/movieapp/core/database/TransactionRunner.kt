package com.movieapp.core.database

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection

/** Runs a block in one database write transaction. Lets mediators be tested without a real database. */
interface TransactionRunner {
    suspend fun <R> run(block: suspend () -> R): R
}

class RoomTransactionRunner(private val database: MovieAppDatabase) : TransactionRunner {
    override suspend fun <R> run(block: suspend () -> R): R =
        database.useWriterConnection { transactor -> transactor.immediateTransaction { block() } }
}
