package th.co.prior.webscraping.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import th.co.prior.webscraping.model.CompanyModel;
import th.co.prior.webscraping.repository.CompanyNativeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Repository
@Slf4j
public class CompanyNativeRepositoryImpl implements CompanyNativeRepository {

    private final JdbcTemplate jdbcTemplate;

    public CompanyNativeRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public int insertFee(List<CompanyModel> companyModels) {
        StringBuilder sb = new StringBuilder();
        StringJoiner sj = new StringJoiner(" , ");
        List<Object> paramList = new ArrayList<>();
        sb.append(" INSERT INTO fee (orderNumber, identity_tax_pay, branch, entrepreneur, address, postalCode, registerDate) ");
        sb.append(" VALUES ");
        for(CompanyModel companyModel: companyModels){
            String values = " (?, ?, ?, ?, ?, ?, ?) ";
            paramList.add(companyModel.getOrderNumber());
            paramList.add(companyModel.getIdentityTaxPay());
            paramList.add(companyModel.getBranch());
            paramList.add(companyModel.getEntrepreneur());
            paramList.add(companyModel.getAddress());
            paramList.add(companyModel.getPostalCode());
            paramList.add(companyModel.getRegisterDate());
            sj.add(values);
        }

        sb.append(sj.toString());

        log.info("{}", sb);

        Integer insertRow = this.jdbcTemplate.update(sb.toString(), paramList.toArray());

        return insertRow;
    }
}