package com.example.playwright_jvm;

import com.example.playwright_jvm.config.BrowserConfig;
import com.example.playwright_jvm.config.BrowserConfigLoader;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Web Testing")
@Feature("Playwright Basic Tests")
public class WikipediaSearchTest {
    static Playwright playwright;
    static Browser chromiumBrowser;
    static Browser firefoxBrowser;
    static Browser webkitBrowser;

    BrowserContext context;
    Page page;
    private String currentBrowserType;

    // Load browser configurations from YAML
    private static final Map<String, BrowserConfig> BROWSER_CONFIGS = BrowserConfigLoader.loadConfigs();

    static Stream<Arguments> browserProvider() {
        return BROWSER_CONFIGS.entrySet().stream()
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue().getDisplayName()));
    }

    @BeforeAll
    static void launchBrowsers() {
        playwright = Playwright.create();
    }

    @AfterAll
    static void closeBrowsers() {
        if (chromiumBrowser != null) chromiumBrowser.close();
        if (firefoxBrowser != null) firefoxBrowser.close();
        if (webkitBrowser != null) webkitBrowser.close();
        if (playwright != null) playwright.close();
        generateAllureReport();
    }

    @BeforeEach
    void createContextAndPage(TestInfo testInfo) {
        currentBrowserType = extractBrowserType(testInfo.getDisplayName());
        context = createBrowserContext(currentBrowserType);
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        try {
            attachScreenshot();
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }

        try {
            if (page != null) page.close();
            if (context != null) context.close();
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }

        closeBrowsersAfterTest();
    }

    private void closeBrowsersAfterTest() {
        try {
            if (chromiumBrowser != null) { chromiumBrowser.close(); chromiumBrowser = null; }
            if (firefoxBrowser != null) { firefoxBrowser.close(); firefoxBrowser = null; }
            if (webkitBrowser != null) { webkitBrowser.close(); webkitBrowser = null; }
        } catch (Exception e) {
            System.err.println("Browser cleanup failed: " + e.getMessage());
        }
    }

    private String extractBrowserType(String displayName) {
        return BROWSER_CONFIGS.entrySet().stream()
                .filter(entry -> displayName.contains(entry.getValue().getDisplayName()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("chromium");
    }

    private BrowserContext createBrowserContext(String browserType) {
        BrowserConfig config = BROWSER_CONFIGS.get(browserType);
        Browser browser = getBrowser(browserType);

        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setViewportSize(config.getWidth(), config.getHeight());

        if (config.isMobile()) {
            options.setDeviceScaleFactor(3.0).setIsMobile(true).setHasTouch(true);
        }

        return browser.newContext(options);
    }

    private Browser getBrowser(String browserType) {
        // Support headless mode for CI/CD - read from system property
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        int slowMo = headless ? 0 : 200;

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(slowMo);

        if (browserType.contains("chromium")) {
            if (chromiumBrowser == null) chromiumBrowser = playwright.chromium().launch(options);
            return chromiumBrowser;
        } else if (browserType.contains("firefox")) {
            if (firefoxBrowser == null) firefoxBrowser = playwright.firefox().launch(options);
            return firefoxBrowser;
        } else if (browserType.contains("webkit")) {
            if (webkitBrowser == null) webkitBrowser = playwright.webkit().launch(options);
            return webkitBrowser;
        }
        throw new IllegalArgumentException("Unknown browser: " + browserType);
    }

    @Story("Search Functionality")
    @Description("Test searching Wikipedia for Playwright")
    @Severity(SeverityLevel.CRITICAL)
    @Link(name = "Wikipedia", url = "https://en.wikipedia.org/")
    @ParameterizedTest(name = "Search Wikipedia for Playwright on {1}")
    @MethodSource("browserProvider")
    void shouldSearchWikipediaForPlaywright(String browserType, String browserName) {
        currentBrowserType = browserType;

        navigateToWikipedia();
        searchForPlaywright();
        waitForNavigationToComplete();
        verifyPlaywrightPage();
        attachPageTitle();
    }

    @Step("Navigate to Wikipedia")
    private void navigateToWikipedia() {
        page.navigate("https://en.wikipedia.org/");
    }

    @Step("Search for Playwright")
    private void searchForPlaywright() {
        Locator searchInput = page.locator("#searchInput");

        if (searchInput.isVisible()) {
            // Desktop version
            searchInput.fill("playwright");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
        } else {
            // Mobile version - open search dialog
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();

            // Wait for dialog and use combobox
            page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Search Wikipedia"))
                    .fill("playwright");
            page.keyboard().press("Enter");
        }
    }

    @Step("Wait for navigation to complete")
    private void waitForNavigationToComplete() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @Step("Verify we reached a Playwright page")
    private void verifyPlaywrightPage() {
        String url = page.url();
        assertTrue(url.matches(".*wiki.*[Pp]laywright.*"),
                "Expected URL to contain 'wiki' and 'Playwright', but got: " + url);
    }

    @Step("Attach page title")
    private void attachPageTitle() {
        String title = page.title();
        Allure.addAttachment("Page Title", "text/plain", title);
    }

    private void attachScreenshot() {
        try {
            if (page != null && !page.isClosed()) {
                byte[] screenshot = page.screenshot();
                Allure.addAttachment("Screenshot", "image/png",
                        new ByteArrayInputStream(screenshot), "png");
            }
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }
    }

    private static void generateAllureReport() {
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "allure:report");
            pb.start();
            System.out.println("Allure report generation started");
        } catch (Exception e) {
            System.err.println("Could not generate Allure report: " + e.getMessage());
        }
    }
}
