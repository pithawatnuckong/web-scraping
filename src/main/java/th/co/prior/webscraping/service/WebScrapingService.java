package th.co.prior.webscraping.service;

import com.sun.jdi.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import th.co.prior.webscraping.model.CompanyModel;
import th.co.prior.webscraping.model.ResponseModel;
import th.co.prior.webscraping.repository.CompanyNativeRepository;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class WebScrapingService {

    private final CompanyNativeRepository companyNativeRepository;

    public WebScrapingService(CompanyNativeRepository companyNativeRepository) {
        this.companyNativeRepository = companyNativeRepository;
    }

    public ResponseModel<Integer> scrapingAndResponse(String id) {
        ResponseModel<Integer> result = new ResponseModel<>();
        result.setStatus(200);
        result.setDescription("ok");

        try {

            String cookie = this.getCookie(id);

            HashMap<String, String> headers = this.getHeaders(cookie);

            Integer numOfPage = this.findAllPage(id, headers);

            List<CompanyModel> companyModels = this.getCompanyModel(id, headers, numOfPage);

            log.info("{}", companyModels);

            Integer insertRow = this.companyNativeRepository.insertFee(companyModels);

            result.setData(insertRow);
        } catch (Exception ex) {
            result.setStatus(500);
            result.setDescription(ex.getMessage());
            log.warn("{}", ex.getMessage());
        }
        return result;
    }

    public String getCookie(String id) {
        System.setProperty("webdriver.chrome.driver", "../../chromedriver.exe");
        try {
            WebDriver chromeDriver = new ChromeDriver();
            chromeDriver.get("https://vsreg.rd.go.th/VATINFOWSWeb/jsp/V001.jsp");
            WebElement textInputElement = chromeDriver.findElement(By.id("txtTin"));
            WebElement buttonElement = chromeDriver.findElement(By.id("btnSearch"));

            textInputElement.sendKeys(id);
            buttonElement.click();
            String cookie = chromeDriver.manage().getCookieNamed("JSESSIONID").toString();
            log.info("{}", cookie);
            return cookie;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new NotFoundException(ex.getMessage());
        }
    }

    public HashMap<String, String> getHeaders(String cookie) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Cookie", cookie);
        headers.put("Host", "vsreg.rd.go.th");
        return headers;
    }

    public Integer findAllPage(String id, HashMap<String, String> headers) {
        try {

            Document docs = Jsoup.connect("https://vsreg.rd.go.th/VATINFOWSWeb/jsp/VATInfoWSServlet")
                    .data("operation", "GotoPage_Click")
                    .data("goto_page", "1")
                    .data("tin", "on")
                    .data("txtTin", id)
                    .headers(headers)
                    .ignoreContentType(true)
                    .method(Connection.Method.POST).execute().parse();

            Elements listOfElement = docs.getElementsByClass("trHeader2");
            Integer lastIndex = listOfElement.size() - 1;
            Element lastElementOfHead = listOfElement.get(lastIndex);
            Integer allPageNumber = Integer.valueOf(lastElementOfHead.getElementsByTag("font").get(1).text());

            return allPageNumber;

        } catch (Exception ex) {

            ex.printStackTrace();
            log.info("{}", ex.getMessage());
            throw new InternalException(ex.getMessage());

        }
    }

    public List<CompanyModel> getCompanyModel(String id, HashMap<String, String> headers, Integer numOfPage) {

        List<CompanyModel> companyModels = new ArrayList<>();

        try {

            for (int p = 0; p < numOfPage; p++) {
                Document document = Jsoup.connect("https://vsreg.rd.go.th/VATINFOWSWeb/jsp/VATInfoWSServlet")
                        .data("operation", "GotoPage_Click")
                        .data("goto_page", String.valueOf(p + 1))
                        .data("tin", "on")
                        .data("txtTin", "0107542000011")
                        .headers(headers)
                        .ignoreContentType(true)
                        .method(Connection.Method.POST).execute().parse();

                Elements dataByMenu0 = document.getElementsByClass("trMenu0");
                Elements dataByMenu1 = document.getElementsByClass("trMenu1");
                List<CompanyModel> companyModelList1 = this.extractAttributeFromElements(dataByMenu0);
                List<CompanyModel> companyModelList2 = this.extractAttributeFromElements(dataByMenu1);
                companyModelList1.addAll(companyModelList2);
                companyModels.addAll(companyModelList1);
            }

            return companyModels;
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info("{}", ex.getMessage());
            throw new NotFoundException(ex.getMessage());
        }
    }

    public List<CompanyModel> extractAttributeFromElements(Elements dataByMenu0) {
        List<CompanyModel> companyModels = new ArrayList<>();
        for(Element element: dataByMenu0){
            CompanyModel companyModel = new CompanyModel();

            Elements listOfTdElements = element.getElementsByTag("td");
            Integer orderNumber = Integer.parseInt(listOfTdElements.get(0).getElementsByTag("font").get(0).text());
            String identityTaxPay = listOfTdElements.get(1).getElementsByTag("font").get(0).text().replaceAll(" ", "");
            String branch = listOfTdElements.get(2).getElementsByTag("font").get(0).text();
            String entrepreneur = listOfTdElements.get(3).getElementsByTag("font").get(0).text();
            String address = listOfTdElements.get(4).getElementsByTag("font").get(0).text();
            String postalCode = listOfTdElements.get(5).getElementsByTag("font").get(0).text();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate registerDate = LocalDate.parse(listOfTdElements.get(6).getElementsByTag("font").get(0).text(), formatter);

            companyModel.setOrderNumber(orderNumber);
            companyModel.setIdentityTaxPay(identityTaxPay);
            companyModel.setBranch(branch);
            companyModel.setEntrepreneur(entrepreneur);
            companyModel.setAddress(address);
            companyModel.setPostalCode(postalCode);
            companyModel.setRegisterDate(registerDate);

            companyModels.add(companyModel);
        }
        return companyModels;
    }

}