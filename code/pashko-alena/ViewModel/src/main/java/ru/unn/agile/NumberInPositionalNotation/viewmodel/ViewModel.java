package ru.unn.agile.NumberInPositionalNotation.viewmodel;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import ru.unn.agile.NumberInPositionalNotation.Model.NumberInPositionalNotation;
import ru.unn.agile.NumberInPositionalNotation.Model.Notation;

import java.util.ArrayList;
import java.util.List;

public class ViewModel {
    private final StringProperty inputNumber = new SimpleStringProperty();

    private final ObjectProperty<ObservableList<Notation>> notations =
            new SimpleObjectProperty<>(FXCollections.observableArrayList(Notation.values()));
    private final ObjectProperty<Notation> inputNotation = new SimpleObjectProperty<>();
    private final ObjectProperty<Notation> outputNotation = new SimpleObjectProperty<>();
    private final BooleanProperty convertDisabled = new SimpleBooleanProperty();
    private final StringProperty inputNotationStr = new SimpleStringProperty();

    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty outputNumber = new SimpleStringProperty();

    private final List<NumberInPosNotationListener> valueChangedListeners = new ArrayList<>();

    public ViewModel() {
        inputNumber.set("");
        inputNotation.set(Notation.BINARY);
        outputNotation.set(Notation.DECIMAL);
        inputNotationStr.set(inputNotation.toString());
        outputNumber.set("");
        status.set(InputStatus.WAITING.toString());

        BooleanBinding couldCalculate = new BooleanBinding() {
            {
                super.bind(inputNumber, inputNotationStr);
            }
            @Override
            protected boolean computeValue() {
                return getInputStatus() == InputStatus.READY;
            }
        };
        convertDisabled.bind(couldCalculate.not());

        final List<StringProperty> fields = new ArrayList<StringProperty>() { {
            add(inputNumber);
            add(inputNotationStr);
        } };

        for (StringProperty field : fields) {
            final NumberInPosNotationListener listener = new NumberInPosNotationListener();
            field.addListener(listener);
            valueChangedListeners.add(listener);
        }
    }

    public void convert() {
        if (convertDisabled.get()) {
            return;
        }
        NumberInPositionalNotation input =
                new NumberInPositionalNotation(inputNumber.get(), inputNotation.get());
        NumberInPositionalNotation output;
        output = input.convertToNotation(outputNotation.get());

        outputNumber.set(output.getValue());
        status.set(InputStatus.SUCCESS.toString());
    }

    public StringProperty inputNumberProperty() {
        return inputNumber;
    }

    public ObjectProperty<Notation> inputNotationProperty() {
        return inputNotation;
    }

    public ObjectProperty<Notation> outputNotationProperty() {
        return outputNotation;
    }

    public StringProperty outputNumberProperty() {
        return outputNumber;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public BooleanProperty convertDisabledProperty() {
        return convertDisabled;
    }

    public ObjectProperty<ObservableList<Notation>> notationsProperty() {
        return notations;
    }

    public final boolean getConvertDisabled() {
        return convertDisabled.get();
    }

    public final String getOutputNumber() {
        return outputNumber.get();
    }

    public final ObservableList<Notation> getNotations() {
        inputNotationStr.set(inputNotation.toString());
        return notations.get();
    }

    public final String getStatus() {
        return status.get();
    }

    private InputStatus getInputStatus() {
        InputStatus inputInputStatus;
        if (inputNumber.get().isEmpty()) {
            inputInputStatus = InputStatus.WAITING;
        } else {
            NumberInPositionalNotation number =
                    new NumberInPositionalNotation(inputNumber.get(), inputNotation.get());
            if (number.checkInputData()) {
                inputInputStatus = InputStatus.READY;
            } else {
                inputInputStatus = InputStatus.BAD_FORMAT;
            }
        }
        return inputInputStatus;
    }


    private class NumberInPosNotationListener implements ChangeListener<String> {
        @Override
        public void changed(final ObservableValue<? extends String> observable,
                            final String oldValue, final String newValue) {
            status.set(getInputStatus().toString());
        }
    }
}

enum InputStatus {
    WAITING("Please provide input data"),
    READY("Press 'Convert' or Enter"),
    BAD_FORMAT("Bad format"),
    SUCCESS("Success");

    private final String name;

    private InputStatus(final String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
