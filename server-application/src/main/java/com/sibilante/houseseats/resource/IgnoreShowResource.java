package com.sibilante.houseseats.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sibilante.houseseats.model.Show;
import com.sibilante.houseseats.service.IgnoreShowRepository;

@RestController
@RequestMapping("/rest/ignore/shows")
public class IgnoreShowResource {
	
	private static final Logger log = LoggerFactory.getLogger(IgnoreShowResource.class);
	@Autowired
	private IgnoreShowRepository ignoreShow;
	
	@GetMapping
    public Iterable<Show> all(){
		log.info("Returning all shows");
		return ignoreShow.findAll();
    }

	@PostMapping
    public Show addShow(@RequestBody Show show){
        return ignoreShow.save(show);
    }
    
    @DeleteMapping("/{id}")
    public void deleteShow(@PathVariable long id) {
    	ignoreShow.deleteById(id);
    }
    
}
