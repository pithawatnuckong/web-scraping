package th.co.prior.webscraping.controller.rest;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import th.co.prior.webscraping.model.ResponseModel;
import th.co.prior.webscraping.service.WebScrapingService;

@RestController
@Slf4j
@RequestMapping("/api")
public class ScrapingRestController {

   private final WebScrapingService scrapingService;

    public ScrapingRestController(WebScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    @GetMapping("/scrape")
    public ResponseModel<Integer> webScraping(@RequestParam("id") String id) {
        return this.scrapingService.scrapingAndResponse(id);
    }
}
