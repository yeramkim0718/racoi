package racoi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import racoi.Dto.*;
import racoi.Repository.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RacoiApplicationTests {
	@Autowired private InternetBuzzRepository internetBuzzRepository;
	//@Autowired private CompreBuzzRepository compreBuzzRepository;
	@Autowired private RelatedWordRepository relatedWordRepository;

	@Autowired private InternetBuzzMappingRepository internetBuzzMappingRepository;
	//@Autowired private CompreBuzzMappingRepository compreBuzzMappingRepository;
	@Autowired private RelatedWordMappingRepository relatedWordMappingRepository;

	@Test
	@Transactional
	public void save() {
		List<InternetBuzz> buzzes = internetBuzzRepository.findAll();

		InternetBuzz testBuzz = buzzes.get(0);
		String content_id = "test_content_id";
		String content_set_id = "test_content_set_id";

		InternetBuzzMapping n_mapping = new InternetBuzzMapping();
		n_mapping.setInternetBuzz(testBuzz);
		n_mapping.setContentId(content_id);
		n_mapping.setContentsetId(content_set_id);

		internetBuzzMappingRepository.save(n_mapping);

		InternetBuzzMapping find = internetBuzzMappingRepository.findFistByContentIdAndContentsetId(content_id, content_set_id);

		assert(content_id.equals(find.getContentId()));
		assert(content_set_id.equals(find.getContentsetId()));

	}


	@Test
	@Transactional
	public void delete() {

		String href = "1004";
		List<InternetBuzzMapping> children= internetBuzzRepository.findById(href).get().getMappings();
		List<Long> ids = new ArrayList<Long>();

		for(InternetBuzzMapping child : children) {
			ids.add(child.getId());
		}

		internetBuzzRepository.deleteById(href);

		internetBuzzRepository.findById(href).get().setMappings(new ArrayList<InternetBuzzMapping>());
		for(Long id : ids) {
			boolean present_child = internetBuzzMappingRepository.findById(id).isEmpty();
			System.out.println(id);
			System.out.println(present_child);
			}
	}


	@Test
	public void test() {

		InternetBuzz buzz = internetBuzzRepository.findById("1004").get();
		System.out.println(buzz.getProgram());
		System.out.println(buzz.getMappings());
		System.out.println(buzz.presentContentIdInMappings("1"));
		System.out.println(buzz.presentContentIdInMappings("tv_2847"));

	}


}
