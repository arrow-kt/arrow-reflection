

package arrow.reflect.compiler.plugin.runners;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link arrow.reflect.compiler.plugin.GenerateTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("src/testData/box")
@TestDataPath("$PROJECT_ROOT")
public class BoxTestGenerated extends AbstractBoxTest {
    @Test
    public void testAllFilesPresentInBox() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("src/testData/box"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JVM_IR, true);
    }

    @Test
    @TestMetadata("decorator_expected_test.kt")
    public void testDecorator_expected_test() throws Exception {
        runTest("src/testData/box/decorator_expected_test.kt");
    }

    @Test
    @TestMetadata("decorator_test.kt")
    public void testDecorator_test() throws Exception {
        runTest("src/testData/box/decorator_test.kt");
    }

    @Test
    @TestMetadata("optics_iterable_test.kt")
    public void testOptics_iterable_test() throws Exception {
        runTest("src/testData/box/optics_iterable_test.kt");
    }

    @Test
    @TestMetadata("optics_test.kt")
    public void testOptics_test() throws Exception {
        runTest("src/testData/box/optics_test.kt");
    }

    @Test
    @TestMetadata("product_test.kt")
    public void testProduct_test() throws Exception {
        runTest("src/testData/box/product_test.kt");
    }

    @Test
    @TestMetadata("product_test_expected.kt")
    public void testProduct_test_expected() throws Exception {
        runTest("src/testData/box/product_test_expected.kt");
    }

    @Test
    @TestMetadata("sample_test.kt")
    public void testSample_test() throws Exception {
        runTest("src/testData/box/sample_test.kt");
    }
}
