package io.smilo.api.rest.service;

import io.smilo.api.AbstractWebSpringTest;
import io.smilo.api.TestUtility;
import io.smilo.api.address.AddressDTO;
import io.smilo.api.address.AddressStore;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionAddressStore;
import io.smilo.api.block.data.transaction.TransactionDTO;
import io.smilo.api.pendingpool.PendingBlockDataPool;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AddressServiceTests extends AbstractWebSpringTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Autowired
    private TestUtility testUtility;

    @Mock
    private AddressStore addressStore;

    @Mock
    private TransactionAddressStore transactionAddressStore;

    @Mock
    private PendingBlockDataPool pendingBlockDataPool;

    private AddressServiceImpl addressService;

    @Before
    public void initializeAddressService() {
        addressService = new AddressServiceImpl();

        addressService.addressStore = addressStore;
        addressService.pendingBlockDataPool = pendingBlockDataPool;
        addressService.transactionAddressStore = transactionAddressStore;
    }

    @Test
    public void shouldReturnTheCorrectAddress() throws Exception {
        AddressDTO address = createDummyAddressDTO();

        Mockito.when(
                addressStore.getByAddress("Address")
        ).thenReturn(address);

        AddressDTO result = addressService.getAddress("Address");

        Assert.assertEquals(result, address);
    }

    @Test
    public void shouldReturnTransactionsForAddressCorrectly() throws Exception {
        for(GetTransactionForAddressTest test : getTransactionForAddressTests()) {
            Mockito.when(
                    pendingBlockDataPool.getPendingTransactionsForAddress("Address")
            ).thenReturn(test.inPending);

            Mockito.when(
                    transactionAddressStore.getTransactionsForAddress("Address", test.outputSkip, test.outputTake, test.inputIsDescending)
            ).thenReturn(test.inDatabase);

            Mockito.when(
                    transactionAddressStore.getTransactionCountForAddress("Address")
            ).thenReturn((long)test.inDatabase.size());

            TransactionList result = addressService.getTransactionsForAddress("Address", test.inputSkip, test.inputTake, test.inputIsDescending);

            Assert.assertEquals(result.getSkip(), test.outputSkip);
            Assert.assertEquals(result.getTake(), test.outputTake);
            Assert.assertEquals(result.getTotalCount(), test.outputTotalCount);

            // We cannot use assertArrayEquals because this would do an object-by-reference comparison.
            // Instead we cast each TransactionDTO back to a Transaction and use the equality check
            // we already build into the Transaction class.
            Assert.assertEquals(result.getTransactions().size(), test.output.size());
            for(int i = 0; i < result.getTransactions().size(); i++) {
                TransactionDTO expected = test.output.get(i);
                TransactionDTO actual = result.getTransactions().get(i);

                Assert.assertEquals(
                    TransactionDTO.toTransaction(expected).equals(TransactionDTO.toTransaction(actual)),
                        true
                );
            }
        }
    }

    private List<GetTransactionForAddressTest> getTransactionForAddressTests() {
        List<GetTransactionForAddressTest> tests = new ArrayList<>();

        // Test #1
        {
            Transaction pending1 = testUtility.createDummyTransaction("DataHash1");
            Transaction pending2 = testUtility.createDummyTransaction("DataHash2");
            Transaction pending3 = testUtility.createDummyTransaction("DataHash3");

            int inputSkip = 0;
            int inputTake = 32;
            boolean inputIsDescending = false;

            long outputTotalCount = 3;
            int outputSkip = 0;
            int outputTake = 32;

            List<TransactionDTO> inDatabase = new ArrayList<>();
            List<Transaction> inPending = Arrays.asList(
                new Transaction[]{pending1, pending2, pending3}
            );

            List<TransactionDTO> output = Arrays.asList(
                new TransactionDTO[]{
                        TransactionDTO.toDTO(pending1),
                        TransactionDTO.toDTO(pending2),
                        TransactionDTO.toDTO(pending3)
                }
            );

            tests.add(
                    new GetTransactionForAddressTest("Test #1",
                            inputSkip, inputTake, inputIsDescending,
                            outputSkip, outputTake, outputTotalCount,
                            inDatabase, inPending,
                            output)
            );
        }

        // Test #2
        {
            TransactionDTO database1 = testUtility.createDummyTransactionDTO("DataHash1");
            TransactionDTO database2 = testUtility.createDummyTransactionDTO("DataHash2");
            TransactionDTO database3 = testUtility.createDummyTransactionDTO("DataHash3");

            int inputSkip = 0;
            int inputTake = 32;
            boolean inputIsDescending = false;

            long outputTotalCount = 3;
            int outputSkip = 0;
            int outputTake = 32;

            List<TransactionDTO> inDatabase = Arrays.asList(
                new TransactionDTO[]{database1, database2, database3}
            );
            List<Transaction> inPending = new ArrayList<>();

            List<TransactionDTO> output = Arrays.asList(
                    new TransactionDTO[]{
                            database1,
                            database2,
                            database3
                    }
            );

            tests.add(
                    new GetTransactionForAddressTest("Test #1",
                            inputSkip, inputTake, inputIsDescending,
                            outputSkip, outputTake, outputTotalCount,
                            inDatabase, inPending,
                            output)
            );
        }

        return tests;
    }

    private AddressDTO createDummyAddressDTO() {
        AddressDTO addressDTO = new AddressDTO();

        addressDTO.setAddress("Address");
        addressDTO.setBalances(new HashMap<>());
        addressDTO.setSignatureCount(0);

        return addressDTO;
    }
}

class GetTransactionForAddressTest {
    public final String name;

    public final int inputSkip;
    public final int inputTake;
    public final boolean inputIsDescending;

    public final long outputTotalCount;
    public final int outputSkip;
    public final int outputTake;

    public final List<TransactionDTO> inDatabase;
    public final List<Transaction> inPending;

    public final List<TransactionDTO> output;

    public GetTransactionForAddressTest(String name,
                                        int inputSkip, int inputTake, boolean inputIsDescending,
                                        int outputSkip, int outputTake, long outputTotalCount,
                                        List<TransactionDTO> inDatabase, List<Transaction> inPending,
                                        List<TransactionDTO> output) {
        this.name = name;

        this.inputSkip = inputSkip;
        this.inputTake = inputTake;
        this.inputIsDescending = inputIsDescending;

        this.outputSkip = outputSkip;
        this.outputTake = outputTake;
        this.outputTotalCount = outputTotalCount;

        this.inDatabase = inDatabase;
        this.inPending = inPending;

        this.output = output;
    }
}
