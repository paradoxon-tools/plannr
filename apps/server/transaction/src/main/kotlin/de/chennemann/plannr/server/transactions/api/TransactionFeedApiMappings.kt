package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedItemResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedItemResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedItemResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedItemResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.domain.AccountFutureTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.AccountTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.PocketFutureTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.PocketTransactionFeedItem
import de.chennemann.plannr.server.transactions.usecases.ListAccountFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListAccountTransactionFeed.Page
import de.chennemann.plannr.server.transactions.usecases.ListPocketFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListPocketTransactionFeed

fun Page.toResponse(): AccountTransactionFeedPageResponse =
    AccountTransactionFeedPageResponse(
        items = items.map { it.toResponse() },
        nextBefore = nextBefore,
    )

fun AccountTransactionFeedItem.toResponse(): AccountTransactionFeedItemResponse =
    AccountTransactionFeedItemResponse(
        accountId = accountId,
        transactionId = transactionId,
        historyPosition = historyPosition,
        transactionDate = transactionDate,
        type = type,
        status = status,
        description = description,
        transactionAmount = transactionAmount,
        signedAmount = signedAmount,
        balanceAfter = balanceAfter,
        partnerId = partnerId,
        partnerName = partnerName,
        sourcePocketId = sourcePocketId,
        sourcePocketName = sourcePocketName,
        sourcePocketColor = sourcePocketColor,
        destinationPocketId = destinationPocketId,
        destinationPocketName = destinationPocketName,
        destinationPocketColor = destinationPocketColor,
        isArchived = isArchived,
    )

fun ListPocketTransactionFeed.Page.toResponse(): PocketTransactionFeedPageResponse =
    PocketTransactionFeedPageResponse(
        items = items.map { it.toResponse() },
        nextBefore = nextBefore,
    )

fun PocketTransactionFeedItem.toResponse(): PocketTransactionFeedItemResponse =
    PocketTransactionFeedItemResponse(
        pocketId = pocketId,
        accountId = accountId,
        contractId = contractId,
        transactionId = transactionId,
        historyPosition = historyPosition,
        transactionDate = transactionDate,
        type = type,
        status = status,
        description = description,
        transactionAmount = transactionAmount,
        signedAmount = signedAmount,
        balanceAfter = balanceAfter,
        partnerId = partnerId,
        partnerName = partnerName,
        transferPocketId = transferPocketId,
        transferPocketName = transferPocketName,
        transferPocketColor = transferPocketColor,
        isArchived = isArchived,
    )

fun ListAccountFutureTransactionFeed.Page.toResponse(): AccountFutureTransactionFeedPageResponse =
    AccountFutureTransactionFeedPageResponse(
        items = items.map { it.toResponse() },
        nextAfter = nextAfter,
    )

fun AccountFutureTransactionFeedItem.toResponse(): AccountFutureTransactionFeedItemResponse =
    AccountFutureTransactionFeedItemResponse(
        accountId = accountId,
        transactionId = transactionId,
        futurePosition = futurePosition,
        transactionDate = transactionDate,
        type = type,
        status = status,
        description = description,
        transactionAmount = transactionAmount,
        signedAmount = signedAmount,
        projectedBalanceAfter = projectedBalanceAfter,
    )

fun ListPocketFutureTransactionFeed.Page.toResponse(): PocketFutureTransactionFeedPageResponse =
    PocketFutureTransactionFeedPageResponse(
        items = items.map { it.toResponse() },
        nextAfter = nextAfter,
    )

fun PocketFutureTransactionFeedItem.toResponse(): PocketFutureTransactionFeedItemResponse =
    PocketFutureTransactionFeedItemResponse(
        pocketId = pocketId,
        accountId = accountId,
        contractId = contractId,
        transactionId = transactionId,
        futurePosition = futurePosition,
        transactionDate = transactionDate,
        type = type,
        status = status,
        description = description,
        transactionAmount = transactionAmount,
        signedAmount = signedAmount,
        projectedBalanceAfter = projectedBalanceAfter,
    )
