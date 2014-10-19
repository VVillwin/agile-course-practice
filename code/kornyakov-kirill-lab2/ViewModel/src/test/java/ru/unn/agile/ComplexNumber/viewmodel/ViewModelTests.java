package ru.unn.agile.ComplexNumber.viewmodel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ViewModelTests {
    private ViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new ViewModel(new MockLogger());
    }

    @After
    public void tearDown() {
        viewModel = null;
    }

    @Test
    public void canSetDefaultValues() {
        assertEquals("", viewModel.re1Property().get());
        assertEquals("", viewModel.im1Property().get());
        assertEquals("", viewModel.re2Property().get());
        assertEquals("", viewModel.im2Property().get());
        assertEquals(ViewModel.Operation.ADD, viewModel.operationProperty().get());
        assertEquals("", viewModel.resultProperty().get());
        assertEquals(Status.WAITING.toString(), viewModel.statusProperty().get());
    }

    @Test
    public void statusIsWaitingWhenCalculateWithEmptyFields() {
        viewModel.calculate();
        assertEquals(Status.WAITING.toString(), viewModel.statusProperty().get());
    }

    @Test
    public void statusIsReadyWhenFieldsAreFill() {
        setInputData();

        assertEquals(Status.READY.toString(), viewModel.statusProperty().get());
    }

    @Test
    public void canReportBadFormat() {
        viewModel.re1Property().set("a");

        assertEquals(Status.BAD_FORMAT.toString(), viewModel.statusProperty().get());
    }

    @Test
    public void statusIsWaitingIfNotEnoughCorrectData() {
        viewModel.re1Property().set("1");

        assertEquals(Status.WAITING.toString(), viewModel.statusProperty().get());
    }

    @Test
    public void calculateButtonIsDisabledInitially() {
        assertFalse(viewModel.isCalculationPossibleProperty().get());
    }

    @Test
    public void calculateButtonIsDisabledWhenFormatIsBad() {
        setInputData();
        viewModel.re1Property().set("trash");

        assertFalse(viewModel.isCalculationPossibleProperty().get());
    }

    @Test
    public void calculateButtonIsDisabledWithIncompleteInput() {
        viewModel.re1Property().set("1");

        assertFalse(viewModel.isCalculationPossibleProperty().get());
    }

    @Test
    public void calculateButtonIsEnabledWithCorrectInput() {
        setInputData();

        assertTrue(viewModel.isCalculationPossibleProperty().get());
    }

    @Test
    public void canSetAddOperation() {
        viewModel.operationProperty().set(ViewModel.Operation.ADD);
        assertEquals(ViewModel.Operation.ADD, viewModel.operationProperty().get());
    }

    @Test
    public void addIsDefaultOperation() {
        assertEquals(ViewModel.Operation.ADD, viewModel.operationProperty().get());
    }

    @Test
    public void operationAddHasCorrectResult() {
        viewModel.re1Property().set("1");
        viewModel.im1Property().set("4");
        viewModel.re2Property().set("-2");
        viewModel.im2Property().set("-2.5");

        viewModel.calculate();

        assertEquals("-1.0 + 1.5i", viewModel.resultProperty().get());
    }

    @Test
    public void canSetSuccessMessage() {
        setInputData();

        viewModel.calculate();

        assertEquals(Status.SUCCESS.toString(), viewModel.statusProperty().get());
    }

    @Test
    public void canSetBadFormatMessage() {
        viewModel.re1Property().set("#selfie");

        assertEquals(Status.BAD_FORMAT.toString(), viewModel.statusProperty().get());
    }

    @Test
    public void statusIsReadyWhenSetProperData() {
        setInputData();

        assertEquals(Status.READY.toString(), viewModel.statusProperty().get());
    }

    @Test
    public void operationMulHasCorrectResult() {
        viewModel.re1Property().set("2");
        viewModel.im1Property().set("3");
        viewModel.re2Property().set("1");
        viewModel.im2Property().set("2");
        viewModel.operationProperty().set(ViewModel.Operation.MULTIPLY);

        viewModel.calculate();

        assertEquals("-4.0 + 7.0i", viewModel.resultProperty().get());
    }

    @Test
    public void operationAddWithNegativeNumbersHasCorrectResult() {
        viewModel.re1Property().set("1.2");
        viewModel.im1Property().set("2.3");
        viewModel.re2Property().set("-10.4");
        viewModel.im2Property().set("-20.5");
        viewModel.operationProperty().set(ViewModel.Operation.ADD);

        viewModel.calculate();

        assertEquals("-9.2 - 18.2i", viewModel.resultProperty().get());
    }

    @Test
    public void viewModelConstructorThrowsExceptionWithNullLogger() {
        try {
            new ViewModel(null);
            fail("Exception wasn't thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("Logger parameter can't be null", ex.getMessage());
        } catch (Exception ex) {
            fail("Invalid exception type");
        }
    }

    @Test
    public void logIsEmptyInTheBeginning() {
        List<String> log = viewModel.getLog();

        assertTrue(log.isEmpty());
    }

    @Test
    public void logContainsProperMessageAfterCalculation() {
        setInputData();
        viewModel.calculate();
        String message = viewModel.getLog().get(0);

        assertTrue(message.matches(".*" + LogMessages.CALCULATE_WAS_PRESSED + ".*"));
    }

    @Test
    public void logContainsInputArgumentsAfterCalculation() {
        setInputData();

        viewModel.calculate();

        String message = viewModel.getLog().get(0);
        assertTrue(message.matches(".*" + viewModel.re1Property().get()
                + ".*" + viewModel.im1Property().get()
                + ".*" + viewModel.re2Property().get()
                + ".*" + viewModel.im1Property().get() + ".*"));
    }

    @Test
    public void argumentsInfoIssProperlyFormatted() {
        setInputData();

        viewModel.calculate();

        String message = viewModel.getLog().get(0);
        assertTrue(message.matches(".*Arguments"
                        + ": Re1 = " + viewModel.re1Property().get()
                        + "; Im1 = " + viewModel.im1Property().get()
                        + "; Re2 = " + viewModel.re2Property().get()
                        + "; Im2 = " + viewModel.im2Property().get() + ".*"));
    }

    @Test
    public void operationTypeIsMentionedInTheLog() {
        setInputData();

        viewModel.calculate();

        String message = viewModel.getLog().get(0);
        assertTrue(message.matches(".*Add.*"));
    }

    @Test
    public void canPutSeveralLogMessages() {
        setInputData();

        viewModel.calculate();
        viewModel.calculate();
        viewModel.calculate();

        assertEquals(3, viewModel.getLog().size());
    }

    @Test
    public void canSeeOperationChangeInLog() {
        setInputData();

        viewModel.onOperationChanged(ViewModel.Operation.ADD, ViewModel.Operation.MULTIPLY);

        String message = viewModel.getLog().get(0);
        assertTrue(message.matches(".*" + LogMessages.OPERATION_WAS_CHANGED + "Mul.*"));
    }

    @Test
    public void operationIsNotLoggedIfNotChanged() {
        viewModel.onOperationChanged(ViewModel.Operation.ADD, ViewModel.Operation.MULTIPLY);

        viewModel.onOperationChanged(ViewModel.Operation.MULTIPLY, ViewModel.Operation.MULTIPLY);

        assertEquals(1, viewModel.getLog().size());
    }

    @Test
    public void argumentsAreCorrectlyLogged() {
        setInputData();

        viewModel.logInput();

        String message = viewModel.getLog().get(0);
        assertTrue(message.matches(".*" + LogMessages.EDITING_FINISHED
                + "Input arguments are: \\["
                + viewModel.re1Property().get() + "; "
                + viewModel.im1Property().get() + "; "
                + viewModel.re2Property().get() + "; "
                + viewModel.im2Property().get() + "\\]"));
    }

    @Test
    public void calculateIsNotCalledWhenButtonIsDisabled() {
        viewModel.calculate();

        assertTrue(viewModel.getLog().isEmpty());
    }

    private void setInputData() {
        viewModel.re1Property().set("1");
        viewModel.im1Property().set("2");
        viewModel.re2Property().set("3");
        viewModel.im2Property().set("4");
    }
}
