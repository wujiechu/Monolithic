package demo.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
@RestController
@RequestMapping("/catalogssss")
public class myCatalogCon {
    @RequestMapping(path = "/v1/catalog", method = RequestMethod.GET, name = "getCatalog")
    public ResponseEntity getCatalog() {
        return new ResponseEntity<>("bongqialaqiala",HttpStatus.OK);
    }
}
