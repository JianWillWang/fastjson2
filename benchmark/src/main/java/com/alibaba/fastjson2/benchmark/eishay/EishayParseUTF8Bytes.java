package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class EishayParseUTF8Bytes {
    static byte[] utf8Bytes;
    static final ObjectMapper mapper = new ObjectMapper();
    static final Gson gson = new Gson();
    static final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());
    static final ObjectReaderProvider featuresProvider;
    static final JSONReader.Context featuresContext;
    static {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.setDisableReferenceDetect(true);
        provider.setDisableJSONB(true);
        provider.setDisableArrayMapping(true);
        provider.setDisableAutoType(true);
        featuresProvider = provider;
        featuresContext = new JSONReader.Context(provider);
        try {
            InputStream is = EishayParseUTF8Bytes.class.getClassLoader().getResourceAsStream("data/eishay_compact.json");
            utf8Bytes = IOUtils.toString(is, "UTF-8").getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.parseObject(utf8Bytes, MediaContent.class));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.parseObject(utf8Bytes, MediaContent.class));
    }

//    @Benchmark
    public void fastjson2_charset(Blackhole bh) {
        bh.consume(JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.ISO_8859_1, MediaContent.class));
    }

    public void fastjson2_features(Blackhole bh) {
        bh.consume(JSON.parseObject(utf8Bytes, MediaContent.class, featuresContext));
    }

    //    @Benchmark
    public void dsljson(Blackhole bh) throws IOException {
        bh.consume(dslJson.deserialize(MediaContent.class, utf8Bytes, utf8Bytes.length));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.readValue(utf8Bytes, MediaContent.class));
    }

    @Benchmark
    public void gson(Blackhole bh) throws Exception {
        bh.consume(gson
                .fromJson(
                        new String(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.UTF_8),
                        MediaContent.class)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseUTF8Bytes.class.getName())
                .exclude(EishayParseUTF8BytesPretty.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
