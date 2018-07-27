package io.smilo.api.rest.service;

import io.smilo.api.AbstractWebSpringTest;
import io.smilo.api.TestUtility;
import io.smilo.api.block.AddResultType;
import io.smilo.api.block.data.AddBlockDataResult;
import io.smilo.api.block.data.transaction.*;
import io.smilo.api.peer.PeerSender;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import io.smilo.api.rest.models.PostTransactionResult;
import io.smilo.api.rest.models.TransactionList;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class TransactionServiceTests extends AbstractWebSpringTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Autowired
    public TestUtility testUtility;

    @Mock
    private PeerSender peerSenderMock;

    @Mock
    private PendingBlockDataPool pendingBlockDataPoolMock;

    @Mock
    private TransactionStore transactionStoreMock;

    private TransactionServiceImpl transactionService;

    @Before
    public void initialiseTransactionService() {
        transactionService = new TransactionServiceImpl();

        transactionService.peerSender = peerSenderMock;
        transactionService.pendingBlockDataPool = pendingBlockDataPoolMock;
        transactionService.transactionStore = transactionStoreMock;
    }

    @Test
    public void shouldReturnSuccessForCorrectTransactions() throws Exception {
        TransactionDTO transaction = testUtility.createDummyTransactionDTO();

        // Mock the addBlockData method
        Mockito.when(
                pendingBlockDataPoolMock.addBlockData(
                        Mockito.any(Transaction.class)
                )
        ).thenReturn(
            new AddBlockDataResult(null, AddResultType.ADDED, "Added")
        );

        PostTransactionResult result = transactionService.putTransaction(transaction);

        Assert.assertEquals(result.getSucceeded(), true);
        Assert.assertEquals(result.getError(), "");
    }

    @Test
    public void shouldReturnFailureForIncorrectTransactions() throws Exception {
        TransactionDTO transaction = testUtility.createDummyTransactionDTO();

        // Mock the addBlockData method
        Mockito.when(
                pendingBlockDataPoolMock.addBlockData(
                        Mockito.any(Transaction.class)
                )
        ).thenReturn(
                new AddBlockDataResult(null, AddResultType.DUPLICATE, "Already added")
        );

        PostTransactionResult result = transactionService.putTransaction(transaction);

        Assert.assertEquals(result.getSucceeded(), false);
        Assert.assertEquals(result.getError(), "Already added");
    }

    @Test
    public void shouldGetTransactionFromDBCorrectly() throws Exception {
        TransactionDTO transaction = testUtility.createDummyTransactionDTO();

        Mockito.when(
                transactionStoreMock.getTransaction("DataHash")
        ).thenReturn(transaction);

        Assert.assertEquals(
                transactionService.get("DataHash"),
                transaction
        );
    }

    @Test
    public void shouldGetTransactionFromPendingPoolCorrectly() throws Exception {
        Transaction transaction = testUtility.createDummyTransaction();

        // Mock store returns null, should trigger a call to the pending pool
        Mockito.when(
                transactionStoreMock.getTransaction("DataHash")
        ).thenReturn(null);

        Mockito.when(
                pendingBlockDataPoolMock.getPendingTransaction("DataHash")
        ).thenReturn(transaction);

        TransactionDTO result = transactionService.get("DataHash");

        Assert.assertEquals(
                transaction.equals(TransactionDTO.toTransaction(result)),
                true
        );
    }

    @Test
    public void shouldReturnNullWhenGettingUnknownTransaction() throws Exception {
        TransactionDTO result = transactionService.get("DataHash");

        Assert.assertEquals(result, null);
    }

    @Test
    public void shouldGetAllTransactionsFromDBCorrectly() throws Exception {
        List<TransactionDTO> transactionDTOList = new ArrayList<>();

        Mockito.when(
                transactionStoreMock.getTransactionCount()
        ).thenReturn(5L);

        Mockito.when(
                transactionStoreMock.getTransactions(0, 32, false)
        ).thenReturn(transactionDTOList);

        TransactionList result = transactionService.getAll(0, 32, false);

        Assert.assertEquals(result.getTransactions(), transactionDTOList);
        Assert.assertEquals(result.getSkip(), 0);
        Assert.assertEquals(result.getTake(), 32);
        Assert.assertEquals(result.getTotalCount(), 5L);
    }
}
