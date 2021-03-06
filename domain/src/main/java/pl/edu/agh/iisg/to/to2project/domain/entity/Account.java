package pl.edu.agh.iisg.to.to2project.domain.entity;

import com.google.common.base.Preconditions;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicObservableValue;
import org.joda.time.LocalDate;
import pl.edu.agh.iisg.to.to2project.domain.utils.ObservableUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

/**
 * @author Wojciech Pachuta.
 */
@Entity
@Table
public class Account extends AbstractEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String namePOJO;

    @Column(name = "initialBalance", nullable = false)
    private BigDecimal initialBalancePOJO;

    @OneToMany(mappedBy = "destinationAccountEntity")
    private Set<InternalTransaction> internalTransactionDestinationSetPOJO;

    @OneToMany(mappedBy = "destinationAccountEntity")
    private Set<ExternalTransaction> externalTransactionDestinationSetPOJO;

    @OneToMany(mappedBy = "sourceAccountEntity")
    private Set<InternalTransaction> internalTransactionSourceSetPOJO;

    @Transient
    private final StringProperty name;

    @Transient
    private final ObjectProperty<BigDecimal> initialBalance;

    @Transient
    private final ObservableSet<InternalTransaction> internalTransactionDestinationSet;

    @Transient
    private final ObservableSet<ExternalTransaction> externalTransactionDestinationSet;

    @Transient
    private final ObservableSet<InternalTransaction> internalTransactionSourceSet;

    @Transient
    private final MonadicObservableValue<BigDecimal> currentBalance;

    Account() {
        super();
        this.name = new SimpleStringProperty();
        this.initialBalance = new SimpleObjectProperty<>();
        this.internalTransactionDestinationSet = FXCollections.observableSet();
        this.externalTransactionDestinationSet = FXCollections.observableSet();
        this.internalTransactionSourceSet = FXCollections.observableSet();
        this.currentBalance = prepareCurrentBalanceProperty();
    }

    public Account(String name, BigDecimal balance) {
        this();
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        Preconditions.checkNotNull(balance);
        setName(name);
        setInitialBalance(balance);
    }

    @PostLoad
    private void initProperties(){
        name.set(namePOJO);
        initialBalance.setValue(initialBalancePOJO);
        internalTransactionDestinationSet.clear();
        internalTransactionDestinationSet.addAll(internalTransactionDestinationSetPOJO);
        externalTransactionDestinationSet.clear();
        externalTransactionDestinationSet.addAll(externalTransactionDestinationSetPOJO);
        internalTransactionSourceSet.clear();
        internalTransactionSourceSet.addAll(internalTransactionSourceSetPOJO);
    }

    @PrePersist
    @PreUpdate
    private void updatePOJOs(){
        namePOJO = name.get();
        initialBalancePOJO = initialBalance.get();
        if(internalTransactionDestinationSetPOJO == null) {
            internalTransactionDestinationSetPOJO = internalTransactionDestinationSet;
        }
        else {
            internalTransactionDestinationSetPOJO.clear();
            internalTransactionDestinationSetPOJO.addAll(internalTransactionDestinationSet);
        }

        if(externalTransactionDestinationSetPOJO == null) {
            externalTransactionDestinationSetPOJO = externalTransactionDestinationSet;
        }
        else {
            externalTransactionDestinationSetPOJO.clear();
            externalTransactionDestinationSetPOJO.addAll(externalTransactionDestinationSet);
        }

        if(internalTransactionSourceSetPOJO == null) {
            internalTransactionSourceSetPOJO = internalTransactionSourceSet;
        }
        else {
            internalTransactionSourceSetPOJO.clear();
            internalTransactionSourceSetPOJO.addAll(internalTransactionSourceSet);
        }
    }

    @Override
    public Observable[] extractObservables() {
        return new Observable[] {name, initialBalance, currentBalance};
    }

    public void setName(String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        this.name.set(name);
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }



    public void setInitialBalance(BigDecimal initialBalance){
        Preconditions.checkNotNull(initialBalance);
        this.initialBalance.set(initialBalance);
    }

    public ReadOnlyObjectProperty<BigDecimal> initialBalanceProperty() {
        return this.initialBalance;
    }

    public MonadicObservableValue<BigDecimal> currentBalanceProperty(){
        return currentBalance;
    }

    private MonadicObservableValue<BigDecimal> prepareCurrentBalanceProperty(){
        ObservableList<InternalTransaction> intTransDest = ObservableUtils
                .observableList(internalTransactionDestinationSet, trans -> new Observable[]
                        {trans.deltaProperty()});
        ObservableList<InternalTransaction> intTransSrc = ObservableUtils
                .observableList(internalTransactionSourceSet, trans -> new Observable[]
                        {trans.deltaProperty()});
        ObservableList<ExternalTransaction> extTransDest = ObservableUtils
                .observableList(externalTransactionDestinationSet, trans -> new Observable[]
                        {trans.deltaProperty()});

        return combineTransactionsIntoBalance(intTransDest, intTransSrc, extTransDest);
    }

    MonadicObservableValue<BigDecimal> calculateBalanceAtInclusive(ObservableValue<LocalDate> date){
        ObservableList<InternalTransaction> intTransDest = ObservableUtils
                .observableList(internalTransactionDestinationSet,
                        trans -> new Observable[] {date, trans.dateProperty(), trans.deltaProperty()})
                .filtered(elem -> !EasyBind.combine(elem.dateProperty(), date,
                        LocalDate::isAfter).get());
        ObservableList<InternalTransaction> intTransSrc = ObservableUtils
                .observableList(internalTransactionSourceSet,
                        trans -> new Observable[] {date, trans.dateProperty(), trans.deltaProperty()})
                .filtered(elem -> !EasyBind.combine(elem.dateProperty(), date,
                        LocalDate::isAfter).get());
        ObservableList<ExternalTransaction> extTransDest = ObservableUtils
                .observableList(externalTransactionDestinationSet,
                        trans -> new Observable[] {date, trans.dateProperty(), trans.deltaProperty()})
                .filtered(elem -> !EasyBind.combine(elem.dateProperty(), date,
                        LocalDate::isAfter).get());

        return combineTransactionsIntoBalance(intTransDest, intTransSrc, extTransDest);
    }

    private MonadicObservableValue<BigDecimal> combineTransactionsIntoBalance(
        ObservableList<InternalTransaction> intTransDest,
        ObservableList<InternalTransaction> intTransSrc,
        ObservableList<ExternalTransaction> extTransDest){

        ObservableList<ObservableValue<BigDecimal>> internalIncomes = EasyBind
                .map(intTransDest, AbstractTransaction::deltaProperty);
        ObservableList<ObservableValue<BigDecimal>> externalIncomes = EasyBind
                .map(extTransDest, AbstractTransaction::deltaProperty);
        ObservableList<ObservableValue<BigDecimal>> internalOutcomes = EasyBind
                .map(intTransSrc, AbstractTransaction::deltaProperty);

        ObservableValue<BigDecimal> internalIncome = EasyBind
                .combine(internalIncomes, stream -> stream.reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
        ObservableValue<BigDecimal> externalIncome = EasyBind
                .combine(externalIncomes, stream -> stream.reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
        ObservableValue<BigDecimal> internalOutcome = EasyBind
                .combine(internalOutcomes, stream -> stream.reduce(BigDecimal::add).orElse(BigDecimal.ZERO));

        return EasyBind.combine(initialBalance, internalIncome, externalIncome, internalOutcome,
                (a, b, c, d) -> a.add(b).add(c).subtract(d));
    }

    void addAsInternalTransactionDestination(InternalTransaction transaction){
        internalTransactionDestinationSet.add(transaction);
    }

    void removeAsInternalTransactionDestination(InternalTransaction transaction){
        internalTransactionDestinationSet.remove(transaction);
    }

    void addAsExternalTransactionDestination(ExternalTransaction transaction){
        externalTransactionDestinationSet.add(transaction);
    }

    void removeAsExternalTransactionDestination(ExternalTransaction transaction){
        externalTransactionDestinationSet.remove(transaction);
    }

    void addAsInternalTransactionSource(InternalTransaction transaction){
        internalTransactionSourceSet.add(transaction);
    }

    void removeAsInternalTransactionSource(InternalTransaction transaction){
        internalTransactionSourceSet.remove(transaction);
    }

    public ObservableSet<InternalTransaction> getInternalTransactionDestinationSet() {
        return FXCollections.unmodifiableObservableSet(internalTransactionDestinationSet);
    }

    public ObservableSet<ExternalTransaction> getExternalTransactionDestinationSet() {
        return FXCollections.unmodifiableObservableSet(externalTransactionDestinationSet);
    }

    public ObservableSet<InternalTransaction> getInternalTransactionSourceSet() {
        return FXCollections.unmodifiableObservableSet(internalTransactionSourceSet);
    }

    @Override
    public String toString() {
        return nameProperty().get();
    }

    public boolean hasTransactions(){
        return !externalTransactionDestinationSet.isEmpty() ||
                !internalTransactionDestinationSet.isEmpty() ||
                !internalTransactionSourceSet.isEmpty();
    }
}
