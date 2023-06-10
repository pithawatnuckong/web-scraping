package th.co.prior.webscraping.repository;

import th.co.prior.webscraping.model.CompanyModel;

import java.util.List;

public interface CompanyNativeRepository {

    int insertFee(List<CompanyModel> companyModels);
}
