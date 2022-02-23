package sdk.models;

public class Goods {
    private String name;
    private int taxable_amount;
    private int tax_exempt_amount;
    private int total_tax_amount;

    public String getName() {
        return name;
    }

    public int getTaxable_amount() {
        return taxable_amount;
    }

    public int getTax_exempt_amount() {
        return tax_exempt_amount;
    }

    public int getTotal_tax_amount() {
        return total_tax_amount;
    }

    public Goods(String name, int taxable_amount, int tax_exempt_amount, int total_tax_amount) {
        this.name = name;
        this.taxable_amount = taxable_amount;
        this.tax_exempt_amount = tax_exempt_amount;
        this.total_tax_amount = total_tax_amount;
    }
}
