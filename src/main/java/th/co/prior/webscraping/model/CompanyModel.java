package th.co.prior.webscraping.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CompanyModel {
    private Integer id;
    private Integer orderNumber;
    private String identityTaxPay;
    private String branch;
    private String entrepreneur;
    private String address;
    private String postalCode;
    private LocalDate registerDate;
}
