package racoi.Job;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import racoi.Dto.*;
import racoi.HttpUtil.HttpUtil;
import racoi.Repository.*;
import racoi.Service.AmazonS3Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j // log 사용을 위한 lombok 어노테이션
@RequiredArgsConstructor // 생성자 DI를 위한 lombok 어노테이션
@Configuration
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory; // 생성자 DI 받음
    private final StepBuilderFactory stepBuilderFactory; // 생성자 DI 받음

    private final InternetBuzzRepository internetBuzzRepository;
    private final CompreBuzzRepository compreBuzzRepository;
    private final RelatedWordRepository relatedWordRepository;

    private final InternetBuzzMappingRepository internetBuzzMappingRepository;
    private final CompreBuzzMappingRepository compreBuzzMappingRepository;
    private final RelatedWordMappingRepository relatedWordMappingRepository;

    private final AmazonS3Service amazonS3Service;

    @Bean
    public Job internet_job() {
        return jobBuilderFactory.get("internet_job")
                .incrementer(new RunIdIncrementer())
                .start(save_file_relatedWord())
                /*.start(crawling_internetBuzz())
                .on("COMPLETED")
                .to(crawling_internetBuzz_detail())

                .from(crawling_internetBuzz())
                .on("*")
                .to(crawling_internetBuzz())

                .from(crawling_internetBuzz_detail())
                .on("COMPLETED")
                .to(crawling_relatedWords())

                .from(crawling_internetBuzz_detail())
                .on("*")
                .to(crawling_internetBuzz_detail())

                .from(crawling_relatedWords())
                .on("COMPLETED")
                .to(updating_related_word_detail())

                .from(crawling_relatedWords())
                .on("*")
                .to(crawling_relatedWords())

                .from(updating_related_word_detail())
                .on("COMPLETED")
                .to(mapping_internetBuzz())

                .from(updating_related_word_detail())
                .on("*")
                .to(updating_related_word_detail())

                .from(mapping_internetBuzz())
                .on("COMPLETED")
                .to(mapping_relatedWord())

                .from(mapping_internetBuzz())
                .on("*")
                .to(mapping_internetBuzz())

                .from(mapping_relatedWord())
                .on("COMPLETED")
                .to(save_file_internetBuzz())

                .from(mapping_relatedWord())
                .on("*")
                .to(mapping_relatedWord())

                .from(save_file_internetBuzz())
                .on("COMPLETED")
                .to(save_file_relatedWord())

                .from(save_file_internetBuzz())
                .on("*")
                .to(save_file_internetBuzz())

                .from(save_file_relatedWord())
                .on("FAILED")
                .to(save_file_relatedWord())

                .from(save_file_relatedWord())
                .on("*")
                .end()
                .end()*/
                .build();

    }
    /*
        @Bean
        public Job compre_job() {
            return jobBuilderFactory.get("compre_job")
                    .incrementer(new RunIdIncrementer())

                    .start(crawling_compreBuzz())
                    .on("COMPLETED")
                    .to(crawling_compreBuzz_detail())

                    .from(crawling_compreBuzz())
                    .on("*")
                    .to(crawling_compreBuzz())

                    .from(crawling_compreBuzz_detail())
                    .on("COMPLETED")
                    .to(mapping_compreBuzz())

                    .from(crawling_compreBuzz_detail())
                    .on("*")
                    .to(crawling_compreBuzz_detail())

                    .from(mapping_compreBuzz())
                    .on("FAILED")
                    .to(mapping_compreBuzz())

                    .from(mapping_compreBuzz())
                    .on("*")
                    .end()
                    .end() // JOB 종료
                    .build();

        }
    */
    private boolean compare_date(JSONObject detail, String buzz_startDate) {
        boolean res = false;

        try {
            Date startDate = new SimpleDateFormat("yyyy.MM.dd").parse(buzz_startDate);
            Date res_date = new SimpleDateFormat("MM/dd/yyyy").parse(detail.getJSONObject("item").getString("conts_release_date"));

            if (startDate.equals(res_date)) {
                res = true;
            }

        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    private boolean compare_casts(JSONObject detail, String buzz_casts, String buzz_directors) {

        boolean res = false;
        int check = 0;

        try {

            HashSet<String> casts = new HashSet<>();
            HashSet<String> directors = new HashSet<>();

            for (String cast : buzz_casts.split(",")) {
                if (!cast.equals("")) {
                    casts.add(cast.replace(" ", ""));
                }
            }

            for (String director : buzz_directors.split(",")) {
                if (!director.equals("")) {
                    directors.add(director);
                }
            }

            JSONObject tab_cast = detail.getJSONObject("item").getJSONObject("item_detail").getJSONObject("tab_cast");
            JSONArray persons = tab_cast.getJSONArray("items");

            for (int idx = 0; idx < persons.length(); idx++) {
                JSONObject person = persons.getJSONObject(idx);
                String name = person.getString("item_name").replace(" ", "");
                String role = person.getString("person_role_type");

                if (role.equals("DIRECTOR")) {
                    if (directors.contains(name)) {
                        check++;
                    }
                } else if (role.equals("ACTOR")) {
                    if (casts.contains(name)) {
                        check++;
                    }
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (check >= 1) {
            res = true;
        }

        return res;
    }

    private String convert_specialChars(String word) {
        String convert = word.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("'","&apos;")
                .replace("\"","&quot;") ;
        return convert;
    }

    @Bean
    public Step mapping_internetBuzz() {
        return stepBuilderFactory.get("mapping_internetBuzz")
                .tasklet((contribution, chunkContext) -> {
                    List<InternetBuzz> buzzes = internetBuzzRepository.findAll();

                    HttpUtil http = new HttpUtil();
                    if (!http.initService(3000, 10)) {
                        System.out.println("call initService fail");
                    }

                    int n_search_call = 0;
                    int n_search_not_ok = 0;
                    int n_detail_call = 0;
                    int n_detail = 0;
                    int n_cast = 0;
                    int n_date = 0;

                    for (InternetBuzz buzz : buzzes) {
                        n_search_call++;

                        JSONObject search = http.call_searchAPI(buzz.getProgram(), 3000, 2);

                        // 응답이 200이 아닌 경우
                        if (search == null) {
                            n_search_not_ok++;
                            continue;
                        }

                        JSONArray results = search.getJSONObject("response").getJSONArray("results");
                        JSONObject ob0 = results.getJSONObject(0);

                        if (ob0.getString("id").equals("tvshow")) {
                            JSONArray doc = ob0.getJSONArray("doc");

                            for (int i = 0; i < doc.length(); i++) {

                                JSONObject json_item = doc.getJSONObject(i);
                                String title = json_item.getString("title");
                                String content_id = json_item.getString("content_id");
                                String content_set_id = json_item.getString("content_set_id");

                                if (buzz.getProgram().replace(" ", "").equals(title.replace(" ", ""))) {
                                    n_detail_call++;

                                    if (!buzz.presentContentIdInMappings(content_id)) {
                                        InternetBuzzMapping n_mapping = new InternetBuzzMapping();
                                        n_mapping.setInternetBuzz(buzz);
                                        n_mapping.setContentId(convert_specialChars(content_id));
                                        n_mapping.setContentsetId(convert_specialChars(content_set_id));
                                        internetBuzzMappingRepository.save(n_mapping);
                                    }

                                    /*JSONObject detail = http.call_detailAPI(content_set_id, content_id, 3000, 2);

                                    if (detail != null && detail.getString("message").equals("OK")) {
                                        n_detail++;
                                        boolean check = buzz.presentContentIdInMappings(content_id);

                                        // release_date가 존재하는 경우 : start_date(InternetBuzz의 값) 와 conts_release_date(응답 값) 일치여부 확인
                                        if (!buzz.getStartDate().equals("")
                                                && detail.getJSONObject("item").has("conts_release_date")
                                                && compare_date(detail, buzz.getStartDate())
                                                && !check) {
                                            n_date++;
                                            InternetBuzzMapping n_mapping = new InternetBuzzMapping();
                                            n_mapping.setInternetBuzz(buzz);
                                            n_mapping.setContentId(content_id);
                                            n_mapping.setContentsetId(content_set_id);
                                            internetBuzzMappingRepository.save(n_mapping);

                                        }
                                        // cast가 존재하는 경우
                                        else if (!(buzz.getCasts().equals("") && buzz.getDirector().equals(""))
                                                && detail.getJSONObject("item").getJSONObject("item_detail").has("tab_cast")
                                                && detail.getJSONObject("item").getJSONObject("item_detail").get("tab_cast").getClass().getName().equals("org.codehaus.jettison.json.JSONObject")
                                                && compare_casts(detail, buzz.getCasts(), buzz.getDirector())
                                                && !check) {

                                            n_cast++;

                                            InternetBuzzMapping n_mapping = new InternetBuzzMapping();
                                            n_mapping.setContentId(content_id);
                                            n_mapping.setContentsetId(content_set_id);
                                            n_mapping.setInternetBuzz(buzz);
                                            internetBuzzMappingRepository.save(n_mapping);

                                        } else {
                                            //System.out.println(content_set_id + "|" + content_id);
                                        }
                                    } else {
                                        //System.out.println(detail);
                                    } */
                                }
                            }
                        }
                    }

                    System.out.println("search api : " + n_search_call);
                    System.out.println("search call not ok : " + n_search_not_ok);
                    System.out.println("detail api : " + n_detail_call);
                    System.out.println("message ok : " + n_detail);
                    System.out.println("release date 존재하는 : " + n_date);
                    System.out.println("cast mapping : " + n_cast);
                    return RepeatStatus.FINISHED;

                }).build();

    }

    @Bean
    public Step mapping_compreBuzz() {
        return stepBuilderFactory.get("mapping_compreBuzz")
                .tasklet((contribution, chunkContext) -> {
                    List<CompreBuzz> buzzes = compreBuzzRepository.findAll();

                    HttpUtil http = new HttpUtil();
                    http.initService(3000, 10);

                    int n_search_call = 0;
                    int n_search_not_ok = 0;
                    int n_detail_call = 0;
                    int n_detail = 0;
                    int n_cast = 0;
                    int n_date = 0;

                    for (CompreBuzz buzz : buzzes) {
                        n_search_call++;

                        JSONObject search = http.call_searchAPI(buzz.getProgram(), 3000, 2);
                        //TimeUnit.SECONDS.sleep(1);

                        // 응답이 200이 아닌 경우
                        if (search == null) {
                            //search = http.call_searchAPI(buzz.getProgram());
                            n_search_not_ok++;
                            continue;
                        }

                        JSONArray results = search.getJSONObject("response").getJSONArray("results");
                        JSONObject ob0 = results.getJSONObject(0);

                        if (ob0.getString("id").equals("tvshow")) {
                            JSONArray doc = ob0.getJSONArray("doc");

                            for (int i = 0; i < doc.length(); i++) {

                                JSONObject json_item = doc.getJSONObject(i);
                                String title = json_item.getString("title");
                                String content_id = json_item.getString("content_id");
                                String content_set_id = json_item.getString("content_set_id");
                                boolean check = buzz.presentContentIdInMappings(content_id);

                                if (buzz.getProgram().replace(" ", "").equals(title.replace(" ", ""))) {
                                    n_detail_call++;

                                    if (!check) {
                                        CompreBuzzMapping mapping = new CompreBuzzMapping();
                                        mapping.setContentId(convert_specialChars(content_id));
                                        mapping.setContentsetId(convert_specialChars(content_set_id));
                                        mapping.setCompreBuzz(buzz);
                                        compreBuzzMappingRepository.save(mapping);
                                    }

                                    /*JSONObject detail = http.call_detailAPI(content_set_id, content_id, 3000, 2);

                                    if (detail != null && detail.getString("message").equals("OK")) {
                                        n_detail++;

                                        // release_date가 존재하는 경우 : start_date(InternetBuzz의 값) 와 conts_release_date(응답 값) 일치여부 확인
                                        if (!buzz.getStartDate().equals("")
                                                && detail.getJSONObject("item").has("conts_release_date")
                                                && compare_date(detail, buzz.getStartDate())
                                                && !check) {
                                            n_date++;
                                            CompreBuzzMapping mapping = new CompreBuzzMapping();
                                            mapping.setContentId(content_id);
                                            mapping.setContentsetId(content_set_id);
                                            mapping.setCompreBuzz(buzz);

                                            if (!buzz.getMappings().contains(mapping)) {
                                                compreBuzzMappingRepository.save(mapping);
                                            }
                                        }
                                        // cast가 존재하는 경우
                                        else if (!(buzz.getCasts().equals("") && buzz.getDirector().equals(""))
                                                && detail.getJSONObject("item").getJSONObject("item_detail").has("tab_cast")
                                                && detail.getJSONObject("item").getJSONObject("item_detail").get("tab_cast").getClass().getName().equals("org.codehaus.jettison.json.JSONObject")
                                                && compare_casts(detail, buzz.getCasts(), buzz.getDirector())
                                                && !check) {

                                            CompreBuzzMapping mapping = new CompreBuzzMapping();
                                            mapping.setContentId(content_id);
                                            mapping.setContentsetId(content_set_id);
                                            mapping.setCompreBuzz(buzz);
                                            if (!buzz.getMappings().contains(mapping)) {
                                                compreBuzzMappingRepository.save(mapping);
                                            }
                                            n_cast++;
                                        } else {
                                            //System.out.println(++ect);
                                            //System.out.println(content_set_id + "|" + content_id);
                                        }

                                        // 기타
                                    } else {
                                        //System.out.println(detail);
                                    }*/
                                }
                            }
                        }
                    }

                    System.out.println("search api : " + n_search_call);
                    System.out.println("search call not ok : " + n_search_not_ok);
                    System.out.println("detail api : " + n_detail_call);
                    System.out.println("message ok : " + n_detail);
                    System.out.println("release date 존재하는 : " + n_date);
                    System.out.println("cast mapping : " + n_cast);
                    return RepeatStatus.FINISHED;

                }).build();
    }

    @Bean
    public Step mapping_relatedWord() {
        return stepBuilderFactory.get("mapping_relatedWord")
                .tasklet((contribution, chunkContext) -> {
                    List<RelatedWord> words = relatedWordRepository.findAll();
                    List<InternetBuzzMapping> iBuzzMapping = internetBuzzMappingRepository.findAll();
                    HashMap<String, InternetBuzzMapping> map = new HashMap<>();

                    for (InternetBuzzMapping mapping : iBuzzMapping) {
                        String href = mapping.getInternetBuzz().getHref();
                        map.put(href, mapping);
                    }

                    for (RelatedWord word : words) {
                        String href = word.getHref();
                        InternetBuzzMapping find = map.get(href);

                        if (find != null) {
                            String contentId = find.getContentId();
                            String contentSetId = find.getContentsetId();

                            if (!word.presentContentIdAndPriorityInMappings(contentId, word.getPriority())) {
                                RelatedWordMapping wordMapping = new RelatedWordMapping();
                                wordMapping.setContentId(convert_specialChars(contentId));
                                wordMapping.setContentsetId(convert_specialChars(contentSetId));
                                wordMapping.setRelatedWord(word);
                                relatedWordMappingRepository.save(wordMapping);
                            }
                        }
                    }
                    return RepeatStatus.FINISHED;

                }).build();
    }

    @Bean
    public Step crawling_internetBuzz() {
        return stepBuilderFactory.get("crawling_internetBuzz")
                .tasklet((contribution, chunkContext) -> {

                    System.setProperty("webdriver.chrome.driver", "chromedriver_win32_96.exe");
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("headless");
                    options.addArguments("--disable-popup-blocking"); // 팝업 무시
                    options.addArguments("--disable-default-apps"); // 기본앱 사용안함
                    WebDriver driver = new ChromeDriver(options);
                    driver.get("https://www.racoi.or.kr/kobaco/nreport/ibuzz.do");

                    WebElement table = driver.findElement(By.id("content_body_list"));
                    List<WebElement> rows = table.findElements(By.tagName("tr"));
                    internetBuzzRepository.deleteAll();

                    for (WebElement row : rows) {

                        InternetBuzz buzz = new InternetBuzz();
                        String program = row.findElement(By.className("m01_tb01")).findElement(By.tagName("a")).getAttribute("title");

                        if (program.startsWith("* ")) {
                            program = program.replace("* ", "");
                        }

                        if (program.startsWith(" * ")) {
                            program = program.replace(" * ", "");
                        }

                        if (program.startsWith(" *")) {
                            program = program.replace(" *", "");
                        }

                        if (program.endsWith("(종영)")) {
                            program = program.replace("(종영)", "");
                        }
                        buzz.setProgram(program);

                        String href = row.findElement(By.className("m01_tb01")).findElement(By.tagName("a")).getAttribute("href");
                        Matcher matcher = Pattern.compile("=.*%27").matcher(href);
                        if (matcher.find()) {
                            href = matcher.group().replace("=", "").replace("%27", "");
                        }
                        buzz.setHref(href);

                        String channel = row.findElement(By.className("m01_tb02")).getText();
                        String days = row.findElement(By.className("m01_tb03")).getText();

                        buzz.setChannel(channel);
                        buzz.setDays(days);
                        buzz.setPost(row.findElement(By.className("m01_tb04")).getText().replace(",", ""));
                        buzz.setComment(row.findElement(By.className("m01_tb05")).getText().replace(",", ""));
                        buzz.setVideoView(row.findElement(By.className("m01_tb06")).getText().replace(",", ""));
                        buzz.setNews(row.findElement(By.className("m01_tb07")).getText().replace(",", ""));
                        buzz.setVideo(row.findElement(By.className("m01_tb08")).getText().replace(",", ""));
                        buzz.setFamily(row.findElements(By.className("m01_tb09")).get(0).getText().replace(",", ""));
                        buzz.setDetail(row.findElements(By.className("m01_tb09")).get(1).getText().replace(",", ""));

                        internetBuzzRepository.save(buzz);

                    }
                    driver.quit();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step crawling_internetBuzz_detail() {
        return stepBuilderFactory.get("crawling_internetBuzz_detail")
                .tasklet((contribution, chunkContext) -> {

                    System.setProperty("webdriver.chrome.driver", "chromedriver_win32_96.exe");
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("headless");
                    options.addArguments("--disable-popup-blocking"); // 팝업 무시
                    options.addArguments("--disable-default-apps"); // 기본앱 사용안함
                    WebDriver driver = new ChromeDriver(options);

                    List<InternetBuzz> buzzes = internetBuzzRepository.findAll();

                    for (InternetBuzz buzz : buzzes) {
                        driver.get("https://www.racoi.or.kr/kobaco/nreport/pjinfo.do?pjseq=" + buzz.getHref());

                        List<WebElement> info1 = driver.findElement(By.id("info_1"))
                                .findElements(By.tagName("tr")).get(1)
                                .findElements(By.tagName("td"));

                        String genre = info1.get(2).getText();
                        String startDate = info1.get(4).getText().replace("-", "");
                        String endDate = info1.get(5).getText();

                        List<WebElement> info2 = driver.findElement(By.id("info_2"))
                                .findElements(By.tagName("tr")).get(1)
                                .findElements(By.tagName("td"));

                        String director = info2.get(0).getText();
                        String writer = info2.get(1).getText();
                        String casts = info2.get(2).getText();

                        buzz.setGenre(genre);
                        buzz.setStartDate(startDate);
                        buzz.setEndDate(endDate);
                        buzz.setDirector(director);
                        buzz.setWriter(writer);
                        buzz.setCasts(casts);

                        internetBuzzRepository.save(buzz);
                    }

                    driver.quit();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step crawling_compreBuzz() {
        return stepBuilderFactory.get("crawling_compreBuzz")
                .tasklet((contribution, chunkContext) -> {
                    System.setProperty("webdriver.chrome.driver", "chromedriver_win32_96.exe");
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("headless");
                    options.addArguments("--disable-popup-blocking"); // 팝업 무시
                    options.addArguments("--disable-default-apps"); // 기본앱 사용안함
                    WebDriver driver = new ChromeDriver(options);
                    driver.get("https://www.racoi.or.kr/kobaco/nreport/totalresponse.do#");

                    WebElement table = driver.findElement(By.id("content_body_list"));
                    List<WebElement> rows = table.findElements(By.tagName("tr"));

                    compreBuzzRepository.deleteAll();

                    for (WebElement row : rows) {

                        CompreBuzz buzz = new CompreBuzz();

                        String href = row.findElement(By.className("m03_tb01")).findElement(By.tagName("a")).getAttribute("href");
                        Matcher matcher = Pattern.compile("=.*%27").matcher(href);
                        if (matcher.find()) {
                            href = matcher.group().replace("=", "").replace("%27", "");
                        }
                        buzz.setHref(href);

                        String program = row.findElement(By.className("m03_tb01")).findElement(By.tagName("a")).getAttribute("title");

                        if (program.startsWith("* ")) {
                            program = program.replace("* ", "");
                        }

                        if (program.startsWith(" * ")) {
                            program = program.replace(" * ", "");
                        }

                        if (program.startsWith(" *")) {
                            program = program.replace(" *", "");
                        }

                        if (program.endsWith("(종영)")) {
                            program = program.replace("(종영)", "");
                        }

                        buzz.setProgram(program);
                        buzz.setStartDate(row.findElement(By.className("m03_tb02")).getText());
                        buzz.setChannel(row.findElement(By.className("m03_tb03")).getText());
                        buzz.setDays(row.findElement(By.className("m03_tb04")).getText());

                        buzz.setPost(row.findElement(By.className("m03_tb07")).getText().replace(",", ""));
                        buzz.setComment(row.findElement(By.className("m03_tb08")).getText().replace(",", ""));
                        buzz.setVideoView(row.findElement(By.className("m03_tb09")).getText().replace(",", ""));

                        buzz.setNews(row.findElement(By.className("m03_tb05")).getText().replace(",", ""));
                        buzz.setVideo(row.findElement(By.className("m03_tb06")).getText().replace(",", ""));

                        buzz.setFamily(row.findElement(By.className("m03_tb10")).getText().replace(",", ""));
                        buzz.setIndividual(row.findElement(By.className("m03_tb11")).getText().replace(",", ""));
                        buzz.setMan(row.findElement(By.className("m03_tb12")).getText().replace(",", ""));
                        buzz.setWoman(row.findElement(By.className("m03_tb13")).getText().replace(",", ""));
                        buzz.setTeenager(row.findElement(By.className("m03_tb14")).getText().replace(",", ""));
                        buzz.setTwenties(row.findElement(By.className("m03_tb15")).getText().replace(",", ""));
                        buzz.setThirties(row.findElement(By.className("m03_tb16")).getText().replace(",", ""));
                        buzz.setFourties(row.findElement(By.className("m03_tb17")).getText().replace(",", ""));
                        buzz.setFifties(row.findElements(By.className("m03_tb18")).get(0).getText().replace(",", ""));
                        buzz.setSixties(row.findElements(By.className("m03_tb18")).get(1).getText().replace(",", ""));
                        buzz.setTvVod(row.findElement(By.className("m03_tb19")).getText().replace(",", ""));

                        buzz.setPcLive(row.findElement(By.className("m03_tb20")).getText().replace(",", ""));
                        buzz.setPcVod(row.findElement(By.className("m03_tb21")).getText().replace(",", ""));

                        buzz.setMobileLive(row.findElement(By.className("m03_tb22")).getText().replace(",", ""));
                        buzz.setMobileVod(row.findElement(By.className("m03_tb23")).getText().replace(",", ""));

                        compreBuzzRepository.save(buzz);
                    }
                    driver.quit();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step crawling_compreBuzz_detail() {
        return stepBuilderFactory.get("crawling_compreBuzz_detail")
                .tasklet((contribution, chunkContext) -> {
                    System.setProperty("webdriver.chrome.driver", "chromedriver_win32_96.exe");
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("headless");
                    options.addArguments("--disable-popup-blocking"); // 팝업 무시
                    options.addArguments("--disable-default-apps"); // 기본앱 사용안함
                    WebDriver driver = new ChromeDriver(options);
                    List<CompreBuzz> buzzes = compreBuzzRepository.findAll();

                    for (CompreBuzz buzz : buzzes) {
                        driver.get("https://www.racoi.or.kr/kobaco/nreport/pjinfo.do?pjseq=" + buzz.getHref());

                        List<WebElement> info1 = driver.findElement(By.id("info_1"))
                                .findElements(By.tagName("tr")).get(1)
                                .findElements(By.tagName("td"));

                        String genre = info1.get(2).getText();
                        String startDate = info1.get(4).getText();
                        String endDate = info1.get(5).getText();

                        List<WebElement> info2 = driver.findElement(By.id("info_2"))
                                .findElements(By.tagName("tr")).get(1)
                                .findElements(By.tagName("td"));

                        String director = info2.get(0).getText();
                        String writer = info2.get(1).getText();
                        String casts = info2.get(2).getText();

                        buzz.setGenre(genre);
                        buzz.setStartDate(startDate);
                        buzz.setEndDate(endDate);
                        buzz.setDirector(director);
                        buzz.setWriter(writer);
                        buzz.setCasts(casts);

                        compreBuzzRepository.save(buzz);
                    }

                    driver.quit();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step crawling_relatedWords() {
        return stepBuilderFactory.get("crawling_relatedWords")
                .tasklet((contribution, chunkContext) -> {

                    System.setProperty("webdriver.chrome.driver", "chromedriver_win32_96.exe");
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("headless");
                    options.addArguments("--disable-popup-blocking"); // 팝업 무시
                    options.addArguments("--disable-default-apps"); // 기본앱 사용안함
                    WebDriver driver = new ChromeDriver(options);
                    WebDriverWait wait = new WebDriverWait(driver, 10);

                    driver.get("https://www.racoi.or.kr/kobaco/nreport/weekmorp.do");

                    WebElement table = driver.findElement(By.id("morplist"));
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("morplist")));
                    List<WebElement> rows = table.findElements(By.tagName("tr"));

                    JavascriptExecutor js = (JavascriptExecutor) driver;

                    for (WebElement row : rows) {
                        String program = row.findElement(By.className("m02b_tb01")).getText();
                        String program_ = program;
                        String channel = row.findElement(By.className("m02b_tb02")).getText();
                        String days = row.findElement(By.className("m02b_tb03")).getText();
                        String pseq = row.findElement(By.className("m02b_tb01")).findElement(By.tagName("a")).getAttribute("href").split(",")[0].replace("javascript:showMG(","");

                        if (program_.startsWith("* ")) {
                            program_ = program.replace("* ", "");
                        }

                        if (program_.startsWith(" * ")) {
                            program_ = program.replace(" * ", "");
                        }

                        if (program_.startsWith(" *")) {
                            program_ = program.replace(" *", "");
                        }

                        if (program_.endsWith("(종영)")) {
                            program_ = program.replace("(종영)", "");
                        }

                        /*형용사 */
                        js.executeScript("makeMorpGraph("+pseq+",'"+program+"', 'Adjective')");

                        WebElement graph_01 = driver.findElement(By.id("graph_01"));
                        List<WebElement> menuitems = graph_01.findElements(By.xpath("//*[starts-with(@role,'menuitem')]"));
                        List<WebElement> e_words = graph_01.findElements(By.tagName("tspan"));
                        List<Double> cnts = new ArrayList<Double>();
                        ArrayList<String> words = new ArrayList<String> ();

                        for(int i =0;i<menuitems.size();i++) {
                            WebElement menuitem = menuitems.get(i);
                            String path = menuitem.findElement(By.tagName("path")).getAttribute("d");
                            String regEx = "L0,[0-9]*.[0-9]*";
                            Pattern pat = Pattern.compile(regEx);
                            Matcher match = pat.matcher(path);

                            if(match.find()) {
                                cnts.add(Double.parseDouble(match.group().replace("L0,","")));
                            }
                        }

                        double min =99999;
                        double max = 0;
                        for (int i =0; i<e_words.size(); i++) {
                            WebElement word = e_words.get(i);

                            Pattern pat1 = Pattern.compile("[0-9]+");
                            Matcher match1 = pat1.matcher(word.getText().replace(",",""));

                            if(match1.find()){
                                int num = Integer.parseInt(match1.group());
                                if (num< min) {
                                    min = num;
                                } else if (num > max) {
                                    max = num;
                                }
                            }

                            Pattern pat2 = Pattern.compile("^[가-힣]+$");
                            Matcher match2 = pat2.matcher(word.getText());

                            if(match2.find()) {
                                words.add(match2.group());
                            }
                        }

                        for (int i =0;i<words.size();i++) {
                            RelatedWord relatedWord = new RelatedWord();
                            relatedWord.setProgram(program_);
                            relatedWord.setChannel(channel);
                            relatedWord.setDays(days);
                            relatedWord.setPriority(String.valueOf(i + 1));
                            relatedWord.setWord(words.get(i));
                            relatedWord.setWordClass("Adjective");
                            relatedWord.setAmount(String.valueOf(((int) ((max-min) * cnts.get(i)/202.0 + min))));
                            relatedWordRepository.save(relatedWord);
                        }

                        /*명사 */
                        js.executeScript("makeMorpGraph("+pseq+",'"+program+"', 'Noun')");

                        graph_01 = driver.findElement(By.id("graph_01"));
                        menuitems = graph_01.findElements(By.xpath("//*[starts-with(@role,'menuitem')]"));
                        e_words = graph_01.findElements(By.tagName("tspan"));
                        cnts = new ArrayList<Double>();
                        words = new ArrayList<String> ();

                        for(int i =0;i<menuitems.size();i++) {
                            WebElement menuitem = menuitems.get(i);
                            String path = menuitem.findElement(By.tagName("path")).getAttribute("d");
                            String regEx = "L0,[0-9]*.[0-9]*";
                            Pattern pat = Pattern.compile(regEx);
                            Matcher match = pat.matcher(path);

                            if(match.find()) {
                                cnts.add(Double.parseDouble(match.group().replace("L0,","")));
                            }
                        }

                        min =99999;
                        max = 0;
                        for (int i =0; i<e_words.size(); i++) {
                            WebElement word = e_words.get(i);

                            Pattern pat1 = Pattern.compile("[0-9]+");
                            Matcher match1 = pat1.matcher(word.getText().replace(",",""));

                            if(match1.find()){
                                int num = Integer.parseInt(match1.group());
                                if (num< min) {
                                    min = num;
                                } else if (num > max) {
                                    max = num;
                                }
                            }

                            Pattern pat2 = Pattern.compile("^[가-힣]+$");
                            Matcher match2 = pat2.matcher(word.getText());

                            if(match2.find()) {
                                words.add(match2.group());
                            }
                        }

                        for (int i =0;i<words.size();i++) {
                            RelatedWord relatedWord = new RelatedWord();
                            relatedWord.setProgram(program_);
                            relatedWord.setChannel(channel);
                            relatedWord.setDays(days);
                            relatedWord.setPriority(String.valueOf(i + 1));
                            relatedWord.setWord(words.get(i));
                            relatedWord.setWordClass("Noun");
                            relatedWord.setAmount(String.valueOf(((int) ((max-min) * cnts.get(i)/202.0 + min))));
                            relatedWordRepository.save(relatedWord);
                        }
                    }

                    driver.quit();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step updating_related_word_detail() {
        return stepBuilderFactory.get("updating_related_word_detail")
                .tasklet((contribution, chunkContext) -> {

                    List<RelatedWord> relatedWords = relatedWordRepository.findAll();
                    for (RelatedWord word : relatedWords) {
                        InternetBuzz buzz = internetBuzzRepository.findFirstByProgramAndChannelAndDays(word.getProgram(), word.getChannel(), word.getDays());
                        if (buzz != null) {
                            word.setHref(buzz.getHref());
                            word.setGenre(buzz.getGenre());
                            word.setStartDate(buzz.getStartDate());
                            word.setEndDate(buzz.getEndDate());
                            word.setDirector(buzz.getDirector());
                            word.setWriter(buzz.getWriter());
                            word.setCasts(buzz.getCasts());
                        }
                        relatedWordRepository.save(word);
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private Element createTextNode (Document doc, String element, String text) {
        Element e = doc.createElement(element);
        e.appendChild(doc.createTextNode(text));
        return e;
    }

    @Bean
    public Step save_file_internetBuzz() {
        return stepBuilderFactory.get("save_file_internetBuzz")
                .tasklet((contribution, chunkContext) -> {

                    List<InternetBuzzMapping> mappings = internetBuzzMappingRepository.findAll();

                    try {
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                        // 루트 엘리먼트
                        Document doc = docBuilder.newDocument();
                        Element root = doc.createElement("data");
                        doc.appendChild(root);

                        for (InternetBuzzMapping mapping : mappings) {
                            InternetBuzz buzz = mapping.getInternetBuzz();

                            // row element
                            Element row = doc.createElement("row");
                            root.appendChild(row);
                            row.appendChild(createTextNode(doc, "href", buzz.getHref()));
                            row.appendChild(createTextNode(doc, "program", buzz.getProgram()));
                            row.appendChild(createTextNode(doc, "content_id", mapping.getContentId()));
                            row.appendChild(createTextNode(doc, "contentset_id", mapping.getContentsetId()));
                            row.appendChild(createTextNode(doc, "channel", buzz.getChannel()));
                            row.appendChild(createTextNode(doc, "days", buzz.getDays()));
                            row.appendChild(createTextNode(doc, "post", buzz.getPost()));
                            row.appendChild(createTextNode(doc, "comment", buzz.getComment()));
                            row.appendChild(createTextNode(doc, "video_view", buzz.getVideoView()));
                            row.appendChild(createTextNode(doc, "news", buzz.getNews()));
                            row.appendChild(createTextNode(doc, "video", buzz.getVideo()));
                            row.appendChild(createTextNode(doc, "family", buzz.getFamily()));
                            row.appendChild(createTextNode(doc, "detail", buzz.getDetail()));
                            row.appendChild(createTextNode(doc, "genre", buzz.getGenre()));
                            row.appendChild(createTextNode(doc, "start_date", buzz.getStartDate()));
                            row.appendChild(createTextNode(doc, "end_date", buzz.getEndDate()));
                            row.appendChild(createTextNode(doc, "director", buzz.getDirector()));
                            row.appendChild(createTextNode(doc, "writer", buzz.getWriter()));
                            row.appendChild(createTextNode(doc, "casts", buzz.getCasts()));

                        }

                        // XML 파일로 쓰기
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();

                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(new FileOutputStream(new File("internetbuzz.xml")));
                        transformer.transform(source, result);

                        // file upload
                        amazonS3Service.upload("internetbuzz.xml");

                    } catch (ParserConfigurationException pce) {
                        pce.printStackTrace();
                    } catch (TransformerException tfe) {
                        tfe.printStackTrace();
                    }
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step save_file_compreBuzz() {
        return stepBuilderFactory.get("save_file_compreBuzz")
                .tasklet((contribution, chunkContext) -> {
                    List<CompreBuzzMapping> mappings = compreBuzzMappingRepository.findAll();

                    try {
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                        // 루트 엘리먼트
                        Document doc = docBuilder.newDocument();
                        Element root = doc.createElement("data");
                        doc.appendChild(root);

                        for (CompreBuzzMapping mapping : mappings) {
                            CompreBuzz buzz = mapping.getCompreBuzz();

                            // row element
                            Element row = doc.createElement("row");
                            root.appendChild(row);

                            row.appendChild(createTextNode(doc, "href", buzz.getHref()));
                            row.appendChild(createTextNode(doc, "program", buzz.getProgram()));
                            row.appendChild(createTextNode(doc, "content_id", mapping.getContentId()));
                            row.appendChild(createTextNode(doc, "contentset_id", mapping.getContentsetId()));
                            row.appendChild(createTextNode(doc, "channel", buzz.getChannel()));
                            row.appendChild(createTextNode(doc, "days", buzz.getDays()));
                            row.appendChild(createTextNode(doc, "post", buzz.getPost()));
                            row.appendChild(createTextNode(doc, "comment", buzz.getComment()));
                            row.appendChild(createTextNode(doc, "video_view", buzz.getVideoView()));
                            row.appendChild(createTextNode(doc, "news", buzz.getNews()));
                            row.appendChild(createTextNode(doc, "video", buzz.getVideo()));
                            row.appendChild(createTextNode(doc, "family", buzz.getFamily()));
                            row.appendChild(createTextNode(doc, "individual", buzz.getIndividual()));
                            row.appendChild(createTextNode(doc, "man", buzz.getMan()));
                            row.appendChild(createTextNode(doc, "woman", buzz.getWoman()));
                            row.appendChild(createTextNode(doc, "teenager", buzz.getTeenager()));
                            row.appendChild(createTextNode(doc, "twenties", buzz.getTwenties()));
                            row.appendChild(createTextNode(doc, "thirties", buzz.getThirties()));
                            row.appendChild(createTextNode(doc, "fourties", buzz.getFourties()));
                            row.appendChild(createTextNode(doc, "fifties", buzz.getFifties()));
                            row.appendChild(createTextNode(doc, "sixties", buzz.getSixties()));
                            row.appendChild(createTextNode(doc, "tv_vod", buzz.getTvVod()));
                            row.appendChild(createTextNode(doc, "pc_live", buzz.getPcLive()));
                            row.appendChild(createTextNode(doc, "pc_vod", buzz.getPcVod()));
                            row.appendChild(createTextNode(doc, "mobile_vod", buzz.getMobileVod()));
                            row.appendChild(createTextNode(doc, "genre", buzz.getGenre()));
                            row.appendChild(createTextNode(doc, "start_date", buzz.getStartDate()));
                            row.appendChild(createTextNode(doc, "end_date", buzz.getEndDate()));
                            row.appendChild(createTextNode(doc, "director", buzz.getDirector()));
                            row.appendChild(createTextNode(doc, "writer", buzz.getWriter()));
                            row.appendChild(createTextNode(doc, "casts", buzz.getCasts()));

                        }

                        // XML 파일로 쓰기
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(new FileOutputStream(new File("comprebuzz.xml")));
                        transformer.transform(source, result);

                        amazonS3Service.upload("comprebuzz.xml");

                    } catch (ParserConfigurationException pce) {
                        pce.printStackTrace();
                    } catch (TransformerException tfe) {
                        tfe.printStackTrace();
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step save_file_relatedWord() {
        return stepBuilderFactory.get("save_file_relatedWord")
                .tasklet((contribution, chunkContext) -> {
                    List<RelatedWordMapping> mappings = relatedWordMappingRepository.findAll();

                    try {
                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                        // 루트 엘리먼트
                        Document doc = docBuilder.newDocument();
                        Element root = doc.createElement("data");
                        doc.appendChild(root);

                        for (RelatedWordMapping mapping : mappings) {
                            RelatedWord word = mapping.getRelatedWord();

                            // row element
                            Element row = doc.createElement("row");
                            root.appendChild(row);

                            row.appendChild(createTextNode(doc, "href", word.getHref()));
                            row.appendChild(createTextNode(doc, "word", word.getWord()));
                            row.appendChild(createTextNode(doc, "word_class", word.getWordClass()));
                            row.appendChild(createTextNode(doc, "content_id", mapping.getContentId()));
                            row.appendChild(createTextNode(doc, "contentset_id", mapping.getContentsetId()));
                            row.appendChild(createTextNode(doc, "program", word.getProgram()));
                            row.appendChild(createTextNode(doc, "priority", word.getPriority()));
                            row.appendChild(createTextNode(doc, "channel", word.getChannel()));
                            row.appendChild(createTextNode(doc, "days", word.getDays()));
                            row.appendChild(createTextNode(doc, "amount",word.getAmount()));
                            row.appendChild(createTextNode(doc, "genre", word.getGenre()));
                            row.appendChild(createTextNode(doc, "start_date", word.getStartDate()));
                            row.appendChild(createTextNode(doc, "end_date", word.getEndDate()));
                            row.appendChild(createTextNode(doc, "director", word.getDirector()));
                            row.appendChild(createTextNode(doc, "writer", word.getWriter()));
                            row.appendChild(createTextNode(doc, "casts", word.getCasts()));

                        }

                        // XML 파일로 쓰기
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(new FileOutputStream(new File("relatedWord.xml")));
                        transformer.transform(source, result);

                        amazonS3Service.upload("relatedWord.xml");

                    } catch (ParserConfigurationException pce) {
                        pce.printStackTrace();
                    } catch (TransformerException tfe) {
                        tfe.printStackTrace();
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}