package io.github.bmd007.wonderland.hesab_ketab;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ContractTestExtension implements AfterAllCallback {

    public static final Boolean GENERATE_TEST_DATA = false;
    public static final String API_CONTRACTS_DIRECTORY_PATH = "src/test/resources/api_contracts/";
    private static final Logger log = LoggerFactory.getLogger(ContractTestExtension.class);
    private final ObjectMapper objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
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

    public void verifyAgainstOrUpdateApiContract(@NotNull Supplier<List<String>> jsons,
                                                 @NotNull String apiContractFileName,
                                                 String... jsonPathsToIgnoreValue) throws InterruptedException {
        if (GENERATE_TEST_DATA) {
            Thread.sleep(5000);
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

    private String formatted(String d) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(d));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> sorted(List<String> data) {
        return data.stream().sorted().toList();
    }
}
