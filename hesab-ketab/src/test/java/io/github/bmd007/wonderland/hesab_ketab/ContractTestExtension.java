package io.github.bmd007.wonderland.hesab_ketab;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public class ContractTestExtension implements AfterAllCallback {

    public static final Boolean GENERATE_TEST_DATA = false;
    public static final String API_CONTRACTS_DIRECTORY_PATH = "src/test/resources/api_contracts/";

    private final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module(), new JavaTimeModule())
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .featuresToEnable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .featuresToEnable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
        .featuresToEnable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    @Override
    public void afterAll(ExtensionContext context) {
        assertFalse(GENERATE_TEST_DATA, "Tests ran with GENERATE_TEST_DATA=true, Set it back to false!");
    }

    public void verifyAgainstOrUpdateApiContract(byte[] json,
                                                 @NotNull String apiContractFileName,
                                                 String... jsonPathsToIgnoreValue) {
        verifyAgainstOrUpdateApiContract(new String(json), apiContractFileName, jsonPathsToIgnoreValue);
    }

    @SneakyThrows
    public void verifyAgainstOrUpdateApiContract(@NotNull Supplier<List<String>> jsons,
                                                 @NotNull String apiContractFileName,
                                                 String... jsonPathsToIgnoreValue) {
        if (GENERATE_TEST_DATA) {
            Thread.sleep(5000); // Give the application some time to generate all expected data
            writeToApiContracts(apiContractFileName, jsons.get());
        } else {
            List<String> expected = getMultilineContract(apiContractFileName);
            List<String> actual = jsons.get();
            verify(expected, actual, jsonPathsToIgnoreValue);
        }
    }

    public void verifyAgainstOrUpdateApiContract(@NotNull String json,
                                                 @NotNull String apiContractFileName,
                                                 String... jsonPathsToIgnoreValue) {
        if (GENERATE_TEST_DATA) {
            writeToApiContracts(apiContractFileName, List.of(json));
        } else {
            String expected = getApiContract(apiContractFileName);
            verify(expected, json, jsonPathsToIgnoreValue);
        }
    }

    private void verify(final String expected, String actual, String... jsonPathsToIgnoreValue) {
        verify(List.of(expected), List.of(actual), jsonPathsToIgnoreValue);
    }

    private void verify(final List<String> expected,
                        List<String> actual,
                        String... jsonPathsToIgnoreValue) {
        //make sure expected and actual are in the same order
        final List<String> sortedExpected = sorted(expected);
        final List<String> sortedActual = sorted(actual);

        final CustomComparator comparator;
        if (jsonPathsToIgnoreValue.length > 0) {
            var customizations = Arrays.stream(jsonPathsToIgnoreValue)
                .map(field -> new Customization(field, (a, b) -> a != null && b != null))
                .toArray(Customization[]::new);
            comparator = new CustomComparator(JSONCompareMode.NON_EXTENSIBLE, customizations);
        } else {
            comparator = new CustomComparator(JSONCompareMode.STRICT);
        }

        assertThat(sortedActual).hasSameSizeAs(sortedExpected);
        IntStream.range(0, sortedExpected.size())
            .forEach(i -> {
                try {
                    JSONAssert.assertEquals(sortedExpected.get(i), sortedActual.get(i), comparator);
                } catch (AssertionError ae) {
                    log.info("Assertion msg: {}", ae.getMessage());
                    log.debug("orderedExpected {} {}", i, sortedExpected.get(i));
                    log.debug("orderedActual {} {}", i, sortedActual.get(i));
                    throw ae;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private String getApiContract(String apiContractFileName) {
        try {
            return Files.readString(Path.of(API_CONTRACTS_DIRECTORY_PATH + apiContractFileName));
        } catch (IOException ioe) {
            log.error("Error reading apiContract file {}", apiContractFileName, ioe);
            return "";
        }
    }

    private List<String> getMultilineContract(String apiContractFileName) {
        try {
            return Files.readAllLines(Path.of(API_CONTRACTS_DIRECTORY_PATH + apiContractFileName));
        } catch (IOException ioe) {
            log.error("Error reading apiContract file {}", apiContractFileName, ioe);
            return List.of();
        }
    }

    private void writeToApiContracts(String file, List<String> data) {
        try {
            Path path = Path.of(API_CONTRACTS_DIRECTORY_PATH + file);
            if (data.size() == 1 && file.endsWith(".json")) {
                Files.writeString(path, formatted(data.getFirst()));
            } else {
                Files.write(path, data);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    @SneakyThrows
    private String formatted(String d) {
        return objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(objectMapper.readTree(d));
    }

    private List<String> sorted(List<String> data) {
        return data.stream().sorted().toList();
    }
}
