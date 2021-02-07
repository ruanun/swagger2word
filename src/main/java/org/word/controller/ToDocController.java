package org.word.controller;

import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Language;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author ruan
 */
@Api(tags = "the to Markdown API")
@Controller
public class ToDocController {

    @Value("${swagger.url}")
    private String swaggerUrl;

    @ApiOperation(value = "将 swagger 文档一键下载为 Markdown 文档")
    @GetMapping("downloadMarkdown")
    public void downloadMarkdown(@RequestParam(required = false) String url, HttpServletResponse response) throws IOException {
        url = StringUtils.defaultIfBlank(url, swaggerUrl);
        //输出Markdown到单文件
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .withOutputLanguage(Language.ZH)
                .withPathsGroupedBy(GroupBy.TAGS)
                .withGeneratedExamples()
                .withoutInlineSchema()
                .build();

        final Path path = Paths.get("markdown/generated/all").toAbsolutePath();
        Swagger2MarkupConverter.from(new URL(url))
                .withConfig(config)
                .build()
                .toFile(path);

        final byte[] array = FileUtils.readFileToByteArray(new File(path.toString() + ".md"));
        response.setContentType("application/octet-stream;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("toMarkdown.md", "utf-8"));
            bos.write(array, 0, array.length);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
