/*
 * Copyright (c) 2012. JSpringBot. All Rights Reserved.
 *
 * See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The JSpringBot licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jspringbot.keyword.selenium;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jspringbot.JSpringBotLogger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class SeleniumHelper {

    private static final JSpringBotLogger LOG = JSpringBotLogger.getLogger(SeleniumHelper.class);

    protected WebDriver driver;

    protected ElementFinder finder;

    protected JavascriptExecutor executor;

    protected boolean cancelOnNextConfirmation = false;

    protected int screenCaptureCtr = 0;

    protected long screenCaptureSeed = System.currentTimeMillis();

    protected File screenCaptureDir;

    public SeleniumHelper() {}

    public SeleniumHelper(WebDriver driver) {
        this.driver = driver;
        this.executor = (JavascriptExecutor) driver;
        this.finder = new ElementFinder(driver);
    }

    public void windowMaximize() {
        driver.manage().window().maximize();
    }

    public void assignIdToElement(Integer id, String locator) {
        LOG.info(String.format("Assigning temporary id %s to element %s", id, locator));
        WebElement el = finder.find(locator);

        // TODO FIX THIS
        executor.executeScript(String.format("arguments[0].id = '%s';", id));
        //self._current_browser().execute_script("arguments[0].id = '%s';" % id, element)
    }

    public void setScreenCaptureDir(File screenCaptureDir) {
        this.screenCaptureDir = screenCaptureDir;

        // create dir if not existing
        if(!screenCaptureDir.isDirectory()) {
            screenCaptureDir.mkdirs();
        }
    }

    public void navigateTo(String url) {
        driver.navigate().to(url);

        LOG.html("Navigated to <a href='%s'>%s</a>", url, url);
    }

    public void closeBrowser() {
        LOG.info("Closing browser");
        driver.close();
    }

    public void quitBrowser() {
        LOG.info("Quiting browser");
        driver.quit();
    }

    public void alertShouldBePresent(String expectedAlertText) {
        String actualAlertText = getAlertMessage();
        if (!StringUtils.equals(actualAlertText, expectedAlertText)) {
            throw new IllegalArgumentException(String.format("Alert text should have been '%s' but was '%s'", expectedAlertText, actualAlertText));
        }
    }

    public String getAlertMessage() {
        return closeAlert();
    }

    public String confirmAction() {
        String text = closeAlert(!cancelOnNextConfirmation);
        cancelOnNextConfirmation = false;

        return text;
    }

    public void chooseCancelOnNextConfirmation() {
        cancelOnNextConfirmation = true;
    }

    public void checkboxShouldBeSelected(String locator){
        LOG.info("Verifying checkbox '%s' is selected.", locator);
        
        WebElement el = getCheckbox(locator);
        if (!el.isSelected()) {
            throw new IllegalArgumentException(String.format("Checkbox '%s' should have been selected but was not", locator));
        }
    }

    public void checkboxShouldNotBeSelected(String locator) {
        LOG.info("Verifying checkbox '%s' is not selected.", locator);
        
        WebElement el = getCheckbox(locator);
        if (el.isSelected()) {
            throw new IllegalArgumentException(String.format("Checkbox '%s' should not have been selected", locator));
        }
    }

    public void chooseFile(String locator, String filePath) {
        File file = new File(filePath);
        if (!file.isFile()) {
            throw new IllegalArgumentException(String.format("File '%s' does not exist on the local file system", filePath));
        }
        WebElement el = finder.find(locator);
        el.sendKeys(filePath);
    }

    public void clickButton(String locator) {
        LOG.info("Clicking button '%s'.", locator);
        WebElement button = finder.find(locator, false, "input");
        if (button == null) {
            button = finder.find(locator, "button");
        }
        button.click();
    }

    public void clickElement(String locator) {
        LOG.info("Clicking element '%s'.", locator);

        finder.find(locator).click();
    }

    public void clickImage(String locator) {
        LOG.info("Clicking image '%s'.", locator);

        WebElement image = finder.find(locator,false,"img");

        if (image == null) {
            image = finder.find(locator,"input");
        }

        image.click();
    }

    public void clickLink(String locator) {
        LOG.info("Clicking link '%s'.", locator);
        WebElement el = finder.find(locator);
        el.click();
    }

    public List<String> getAllLinks() {
        List<String> links = new ArrayList<String>();

        WebElement el = finder.find("tag=a",false,"a");
        links.add(el.getAttribute("id"));
        return links;
    }

    public void captureScreenShot() throws IOException {
        byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

        FileOutputStream out = null;
        try {
            File file = newScreenCaptureFile();

            LOG.html("Screen captured (%d): <br /> <img src='%s'/>", screenCaptureCtr, file.getName());

            out = new FileOutputStream(file);
            IOUtils.write(bytes, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void deleteAllCookies() {
        Set<Cookie> allCookies = getCookies();
        for (Cookie loadedCookie : allCookies) {
            deleteCookie(loadedCookie);
        }
    }

    public void deleteCookie(String cookieName) {
        LOG.info("Deleting cookie with name %s.", cookieName);

        Cookie cookie = driver.manage().getCookieNamed(cookieName);
        driver.manage().deleteCookie(cookie);
    }

    public String getCookieValue (String cookieName) {
        Cookie cookie = driver.manage().getCookieNamed(cookieName);

        if (cookie != null) {
            LOG.info("Deleting cookie with name %s.", cookieName);
            return cookie.getValue();
        } else {
            throw new IllegalStateException(String.format("Cookie with name '%s' not found", cookieName));
        }
    }

    public Set<Cookie> getCookies () {
        return driver.manage().getCookies();
    }

    public void doubleClickElement(String locator) {
        LOG.info("Double clicking element '%s'.", locator);
        WebElement el = finder.find(locator);
        new Actions(driver).doubleClick(el).perform();
    }

    public void dragAndDrop(String locatorSrc, String locatorDest) {
        WebElement element = finder.find(locatorSrc);
        WebElement target = finder.find(locatorDest);

        new Actions(driver).dragAndDrop(element, target).perform();
    }

    public void dragAndDropByOffset(String locatorSrc, int xOffset, int yOffset) {
        WebElement element = finder.find(locatorSrc);

        new Actions(driver).dragAndDropBy(element, xOffset, yOffset).perform();
    }

    public void elementShouldBeDisabled(String locator) {
        if (isEnabled(locator))  {
            throw new AssertionError(String.format("Element '%s' is enabled", locator));
        }
    }

    public void elementShouldBeEnabled(String locator) {
        if (!isEnabled(locator))  {
            throw new AssertionError(String.format("Element '%s' is disabled", locator));
        }
    }

    public void elementShouldBeVisible(String locator) {
        LOG.info("Verifying element '%s' is visible.", locator);

        if (!isVisible(locator)) {
            throw new AssertionError(String.format("The element '%s' should be visible, but it is not.", locator));
        }
    }

    public void elementShouldNotBeVisible(String locator) {
        LOG.info("Verifying element '%s' is not visible.", locator);

        if (isVisible(locator)) {
            throw new AssertionError(String.format("The element '%s' should not be visible, but it is not.", locator));
        }
    }

    public void currentFrameShouldContain(String text) {
        if (!textIsPresent(text)) {
            throw new AssertionError(String.format("Page should have contained text %s but did not", text));
        }

        LOG.info("Current page contains text %s.", text);
    }

    public void frameShouldContainText(String locator, String text) {
        if (!frameContains(locator, text)) {
            throw new AssertionError(String.format("Page should have contained text '%s' but did not.", text));
        }
        LOG.info(String.format("Current page contains text '%s'.", text));
    }

    public void elementShouldContain(String locator, String expected) {
        LOG.info(String.format("Verifying element '%s' contains text '%s'.", locator, expected));
        String actual = getText(locator,false);
        if (!expected.equalsIgnoreCase(actual)) {
            throw new AssertionError(String.format("Element '%s' should have contained text '%s' but its text was '%s'.", locator, expected, actual));
        }
    }

    public void elementShouldContainClass(String locator, String expectedClassName) {
        LOG.info(String.format("Verifying element '%s' contains class '%s'.", locator, expectedClassName));
        WebElement el = finder.find(locator, true);

        String classNames = el.getAttribute("class");

        if(StringUtils.isNotEmpty(classNames)) {
            if(Arrays.asList(StringUtils.split(classNames, " ")).contains(expectedClassName)) {
                return;
            }
        }

        throw new AssertionError(String.format("Element '%s' should have contained class '%s' but its class was '%s'.", locator, expectedClassName, classNames));
    }

    public void elementShouldNotContainClass(String locator, String expectedClassName) {
        LOG.info(String.format("Verifying element '%s' should not contains class '%s'.", locator, expectedClassName));
        WebElement el = finder.find(locator, true);

        String classNames = el.getAttribute("class");

        if(StringUtils.isNotEmpty(classNames)) {
            if(Arrays.asList(StringUtils.split(classNames, " ")).contains(expectedClassName)) {
                throw new AssertionError(String.format("Element '%s' should have not contained class '%s'.", locator, expectedClassName));
            }
        }
    }

    public void elementTextShouldBe(String locator, String expectedText) {
        LOG.info(String.format("Verifying element %s contains exactly text %s", locator, expectedText));
        WebElement el = finder.find(locator);
        String actualText = el.getText();
        if (!StringUtils.equals(expectedText, actualText)) {
            throw new AssertionError(String.format("The text of element '%s' should have been '%s' but was '%s'.", locator, expectedText, actualText));
        }
    }

    public Object executeJavascript(String code) {
        return executor.executeScript(code);
    }

    public String getElementAttribute(String attributeLocator) {
        String[] parts = parseAttributeLocator(attributeLocator);
        String locator = parts[0];
        String attributeName = parts[1];
        WebElement el =finder.find(parts[0],true);

        if (el == null) {
            throw new IllegalArgumentException(String.format("Element '%s' not found", locator));
        }
        return el.getAttribute(attributeName);
    }

    public int getHorizontalPosition(String locator) {
        WebElement element = finder.find(locator);

        return element.getLocation().getX();
    }

    public int getVerticalPosition(String locator) {
        WebElement element = finder.find(locator);

        return element.getLocation().getY();
    }

    public String getLocation() {
        return driver.getCurrentUrl();
    }

    public int getMatchingCSSCount(String cssLocator) {
        int count = driver.findElements(By.cssSelector(cssLocator)).size();

        LOG.info("Matching css count for '%s' is '%d'", cssLocator, count);

        return count;
    }

    public int getMatchingXpathCount(String xpath) {
        int count = driver.findElements(By.xpath(xpath)).size();

        LOG.info("Matching xpath count for '%s' is '%d'", xpath, count);

        return count;
    }

    public String getSource() {
        return driver.getPageSource();
    }

    public List<String> getListItems(String locator) {
        List<WebElement> selectOptions = getSelectListOptions(locator);
        return getLabelsForOptions(selectOptions);
    }

    public String getSelectedListLabel (String locator) {
        List<String> selectedLabels = getSelectedListLabels(locator);

        if (selectedLabels.size() != 1) {
            throw new IllegalArgumentException(String.format("Select list with locator '%s' does not have a single selected value", locator));
        }

        return selectedLabels.get(1);
    }

    public String getSelectedListValue(String locator) {
        List<String> selectedListValues = getSelectedListValues(locator);

        if (selectedListValues.size() != 1) {
            throw new IllegalArgumentException(String.format("Select list with locator '%s' does not have a single selected value",locator));
        }

        return selectedListValues.get(1);
    }


    public List<String> getSelectedListValues(String locator) {
        List<WebElement> selectedOptions = getSelectListOptionsSelected(locator);

        if (selectedOptions.size() == 0) {
            throw new IllegalArgumentException(String.format("Select list with locator '%s' does not have any selected values",locator));
        }
        return getValuesForOptions(selectedOptions);

    }

    public List<String> getSelectedListLabels(String locator) {
        List<WebElement> selectedOptions = getSelectListOptionsSelected(locator);

        if (selectedOptions.size() == 0) {
            throw new IllegalArgumentException(String.format("Select list with locator %s does not have any selected values.", locator));
        }

        return getLabelsForOptions(selectedOptions);
    }

    public void selectAllFromList(String locator) {
        LOG.info(String.format("Selecting all options from list '%s'.", locator));

        WebElement selectEl = getSelectList(locator);

        if (!isMultiSelectList(selectEl)) {
            throw new IllegalArgumentException("Keyword 'Select all from list' works only for multiselect lists.");
        }

        List<WebElement> selectOptions = getSelectListOptions(selectEl);
        for(WebElement option : selectOptions) {
            if(!option.isSelected()) {
                option.click();
            }
        }
    }

    public void selectFromList(String locator, String... items) {

        LOG.info(String.format("Selecting %s from list '%s'.", items.toString(), locator));

        List<WebElement> selectOptions = getSelectListOptions(locator);
        for (int i=0; i<items.length; i++) {
            String item = items[i];
        }

        /*
        def select_from_list(self, locator, *items):
        """Selects `*items` from list identified by `locator`

        If more than one value is given for a single-selection list, the last
        value will be selected. If the target list is a multi-selection list,
        and `*items` is an empty list, all values of the list will be selected.

        Select list keywords work on both lists and combo boxes. Key attributes for
        select lists are `id` and `name`. See `introduction` for details about
        locating elements.
        """
        items_str = items and "option(s) '%s'" % ", ".join(items) or "all options"
        self._info("Selecting %s from list '%s'." % (items_str, locator))
        items = list(items)

        select, options = self._get_select_list_options(locator)
        is_multi_select = self._is_multiselect_list(select)
        select_func = self._select_option_from_multi_select_list if is_multi_select else self._select_option_from_single_select_list

        if not items:
            for i in range(len(options)):
                select_func(select, options, i)
            return

        option_values = self._get_values_for_options(options)
        option_labels = self._get_labels_for_options(options)
        for item in items:
            option_index = None
            try: option_index = option_values.index(item)
            except:
                try: option_index = option_labels.index(item)
                except: continue
            select_func(select, options, option_index)
         */
    }

    public String getText(String locator) {
        return getText(locator,true);
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getValue(String locator){
        WebElement el = finder.find(locator);

        return el.getAttribute("value");
    }

    public void goBack() {
        driver.navigate().back();
    }

    public void inputPassword(String locator, String password) {
        LOG.info(String.format("Typing password into text field '%s'", locator));
        inputTextIntoTextField(locator, password);
    }

    public void inputText(String locator, String text) {
        LOG.info(String.format("Typing text '%s' into text field '%s'", text, locator));
        inputTextIntoTextField(locator, text);
    }

    public void listSelectionShouldBe() {
        /*
        def list_selection_should_be(self, locator, *items):
        """Verifies the selection of select list identified by `locator` is exactly `*items`.

        If you want to test that no option is selected, simply give no `items`.

        Select list keywords work on both lists and combo boxes. Key attributes for
        select lists are `id` and `name`. See `introduction` for details about
        locating elements.
        """
        items_str = items and "option(s) [ %s ]" % " | ".join(items) or "no options"
        self._info("Verifying list '%s' has %s selected." % (locator, items_str))
        items = list(items)
        self.page_should_contain_list(locator)
        select, options = self._get_select_list_options_selected(locator)
        if not items and len(options) == 0:
            return
        selected_values = self._get_values_for_options(options)
        selected_labels = self._get_labels_for_options(options)
        err = "List '%s' should have had selection [ %s ] but it was [ %s ]" \
            % (locator, ' | '.join(items), ' | '.join(selected_labels))
        for item in items:
            if item not in selected_values + selected_labels:
                raise AssertionError(err)
        for selected_value, selected_label in zip(selected_values, selected_labels):
            if selected_value not in items and selected_label not in items:
                raise AssertionError(err)
         */
    }
    public void mouseDown(String locator) {
        LOG.info(String.format("Simulating Mouse Down on element '%s'", locator));
        WebElement el = finder.find(locator);

        if (el == null) {
            throw new IllegalStateException(String.format("ERROR: Element %s not found", locator));
        }

        new Actions(driver).clickAndHold(el).release().perform();
    }

    public void mouseDownOnImage(String locator){
        WebElement el = finder.find(locator, "image");

        new Actions(driver).clickAndHold(el).perform();
    }
    public void mouseDownOnLink(String locator) {
        WebElement el = finder.find(locator, "link");

        new Actions(driver).clickAndHold(el).perform();
    }

    public void mouseOut(String locator) {
        LOG.info(String.format("Simulating Mouse Over on element '%s'", locator));
        WebElement el = finder.find(locator);

        if (el == null) {
            throw new IllegalStateException(String.format("ERROR: Element %s not found", locator));
        }

        Dimension size = el.getSize();
        int offsetx = (el.getSize().getWidth() / 2 ) + 1;
        int offsety = (el.getSize().getHeight() / 2 ) + 1;

        new Actions(driver).moveToElement(el).moveByOffset(offsetx, offsety).perform();
    }

    public void mouseOver(String locator) {
        LOG.info(String.format("Simulating Mouse Over on element '%s'", locator));
        WebElement el = finder.find(locator);

        if (el == null) {
            throw new IllegalStateException(String.format("ERROR: Element %s not found", locator));
        }

        new Actions(driver).moveToElement(el).perform();
    }

    public void mouseUp(String locator) {
        LOG.info(String.format("Simulating Mouse Up on element '%s'", locator));
        WebElement el = finder.find(locator);

        if (el == null) {
            throw new IllegalStateException(String.format("ERROR: Element %s not found", locator));
        }

        new Actions(driver).clickAndHold(el).release().perform();
    }

    public void pageShouldContain(String text) {
        if (pageContains(text)) {
            LOG.info(String.format("Current page contains text '%s'.", text));
        } else {
            throw new AssertionError(String.format("Page should have contained text %s but did not.", text));
        }
    }

    public void pageShouldContainButton(String locator) {
        if(!isElementPresent(locator, "button")) {
            if(!isElementPresent(locator, "input")) {
                throw new AssertionError(String.format("Page should have contained button '%s' but did not", locator));
            }
        }

        LOG.info(String.format("Current page contains button '%s'.", locator));
    }

    public void pageShouldContainCheckbox(String locator) {
        if(!isElementPresent(locator, "input", "type", "checkbox")) {
            throw new AssertionError(String.format("Page should have contained checkbox '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains checkbox '%s'.", locator));
    }

    public void pageShouldContainElement(String locator) {
        if(!isElementPresent(locator)) {
            throw new AssertionError(String.format("Page should have contained element '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains element '%s'.", locator));
    }

    public void pageShouldContainImage(String locator) {
        if(!isElementPresent(locator,"img")) {
            throw new AssertionError(String.format("Page should have contained image '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains image '%s'.", locator));
    }

    public void pageShouldContainLink(String locator) {
        if(!isElementPresent(locator,"a")) {
            throw new AssertionError(String.format("Page should have contained link '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains link '%s'.", locator));
    }

    public void pageShouldContainList(String locator) {
        if(!isElementPresent(locator,"select")) {
            throw new AssertionError(String.format("Page should have contained list '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains list '%s'.", locator));
    }

    public void pageShouldContainRadio(String locator) {
        if(!isElementPresent(locator, "input", "type", "radio")) {
            throw new AssertionError(String.format("Page should have contained radio '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains radio '%s'.", locator));
    }

    public void pageShouldContainTextfield(String locator) {
        if(!isElementPresent(locator, "input", "type", "text")) {
            throw new AssertionError(String.format("Page should have contained textfield '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains textfield '%s'.", locator));
    }

    public void pageShouldContainPassword(String locator) {
        if(!isElementPresent(locator, "input", "type", "password")) {
            throw new AssertionError(String.format("Page should have contained password field '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains password field '%s'.", locator));
    }

    public void pageShouldNotContain(String text) {
        if (pageContains(text)) {
            throw new AssertionError(String.format("Page should have contained text %s but did not.", text));
        } else {
            LOG.info(String.format("Current page contains text '%s'.", text));
        }
    }

    public void pageShouldNotContainButton(String locator) {
        if(isElementPresent(locator, "button") || isElementPresent(locator, "input")) {
            throw new AssertionError(String.format("Page should have contained button '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains button '%s'.", locator));
    }

    public void pageShouldNotContainCheckbox(String locator) {
        if(isElementPresent(locator, "input", "type", "checkbox")) {
            throw new AssertionError(String.format("Page should have contained checkbox '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains checkbox '%s'.", locator));
    }

    public void pageShouldNotContainElement(String locator) {
        if(isElementPresent(locator)) {
            throw new AssertionError(String.format("Page should have contained element '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains element '%s'.", locator));
    }

    public void pageShouldNotContainImage(String locator) {
        if(isElementPresent(locator,"img")) {
            throw new AssertionError(String.format("Page should have contained image '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains image '%s'.", locator));
    }

    public void pageShouldNotContainLink(String locator) {
        if(isElementPresent(locator,"a")) {
            throw new AssertionError(String.format("Page should have contained link '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains link '%s'.", locator));
    }

    public void pageShouldNotContainList(String locator) {
        if(isElementPresent(locator,"select")) {
            throw new AssertionError(String.format("Page should have contained list '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains list '%s'.", locator));
    }

    public void pageShouldNotContainRadio(String locator) {
        if(isElementPresent(locator, "input", "type", "radio")) {
            throw new AssertionError(String.format("Page should have contained radio '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains radio '%s'.", locator));
    }

    public void pageShouldNotContainTextfield(String locator) {
        if(isElementPresent(locator, "input", "type", "text")) {
            throw new AssertionError(String.format("Page should have contained textfield '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains textfield '%s'.", locator));
    }

    public void pageShouldNotContainPassword(String locator) {
        if(isElementPresent(locator, "input", "type", "password")) {
            throw new AssertionError(String.format("Page should have contained password field '%s' but did not", locator));
        }

        LOG.info(String.format("Current page contains password field '%s'.", locator));
    }

    public void pressKey(String locator, String key) {
        if (key.startsWith("\\") && key.length() > 1) {
            int keyCode = Integer.valueOf(key.substring(2));
            key = mapAsciiKeyCodeToKey(keyCode) ;
        }

        if (key.length() > 1) {
            throw new IllegalArgumentException(String.format("Key value '%s' is invalid.", key));
        }

        WebElement el = finder.find(locator);
        el.sendKeys(key);
    }

    public void reloadPage() {
        driver.navigate().refresh();
    }

    public void radioButtonShouldBeSetTo(String groupName, String valueSelected) {
        LOG.info(String.format("Verifying radio button '%s' has selection '%s'", groupName, valueSelected));

        List<WebElement> els = getRadioButton(groupName);
        String actualValueSelected = getValueFromRadioButtons(els);

        if (actualValueSelected == null || !actualValueSelected.equalsIgnoreCase(valueSelected)) {
            throw new AssertionError(String.format("Selection of radio button '%s' should have been '%s' but was '%s'", groupName, valueSelected, actualValueSelected));
        }
    }

    public void radioButtonShouldNotBeSelected(String groupName) {
        LOG.info(String.format("Verifying radio button '%s' has no selection", groupName));

        List<WebElement> els = getRadioButton(groupName);
        String actualValue = getValueFromRadioButtons(els);

        if (actualValue != null) {
            throw new AssertionError(String.format("Radio button group '%s' should not have had selection, but '%s' was selected", groupName, actualValue));
        }
    }

    public void selectRadioButton(String groupName, String value) {
        LOG.info(String.format("Selecting '%s' from radio button '%s'", value, groupName));

        WebElement el = getRadioButtonWithValue(groupName, value);
        if (!el.isSelected()) {
            el.click();
        }
    }

    public void selectCheckbox(String locator) {
        LOG.info(String.format("Selecting checkbox '%s'.", locator));

        WebElement el = finder.find(locator, true, "input");
        if (!el.isSelected()) {
            el.click();
        }
    }

    public void unselectCheckbox(String locator) {
        LOG.info("Unselecting checkbox '%s'.", locator);

        WebElement el = getCheckbox(locator);
        if (el.isSelected()) {
            el.click();
        }
    }

    public void selectFrame(String locator) {
        LOG.info(String.format("Selecting frame '%s'.", locator));
        WebElement el = finder.find(locator);
        driver.switchTo().frame(el);
    }

    public void unselectFrame() {
        driver.switchTo().defaultContent();
    }

    public void submitForm(String locator) {
        LOG.info("Submitting form '%s'.", locator);

        WebElement el = finder.find(locator, "form");
        el.submit();
    }

    public void textfieldValueShouldBe(String locator, String expectedValue) {
        WebElement el = finder.find(locator,"input");

        String actual = null;
        if (el == null) {
            el = finder.find(locator,"textarea");
        }

        if (el != null) {
            actual = el.getAttribute("value");
        }

        if (!StringUtils.equalsIgnoreCase(actual, expectedValue)) {
            throw new AssertionError(String.format("Value of text field '%s' should have been '%s' but was '%s'", locator, expectedValue, actual));
        }

        LOG.info(String.format("Content of text field '%s' is '%s'.", locator, expectedValue));
    }

    public void titleShouldBe(String title) {
        if (!driver.getTitle().equalsIgnoreCase(title)) {
            throw new AssertionError(String.format("Title should have been '%s' but was '%s'", title, driver.getTitle()));
        }

        LOG.info(String.format("Page title is '%s'.", title));
    }

    public void delay(long pollMillis) {
        try {
            Thread.sleep(pollMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitTillElementFound(String locator, long pollMillis, long timeoutMillis) {
        long start = System.currentTimeMillis();
        long elapse = -1;
        WebElement el;

        do {
            if(elapse != -1) {
                try {
                    Thread.sleep(pollMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            el = finder.find(locator, false);
            elapse = System.currentTimeMillis() - start;
        } while(el == null && elapse < timeoutMillis);

        if(el == null) {
            throw new IllegalStateException(String.format("timeout for locating '%s' (%d ms) reached.", locator, timeoutMillis));
        }
    }

    public void waitTillElementVisible(String locator, long pollMillis, long timeoutMillis) {
        long start = System.currentTimeMillis();
        long elapse = -1;
        WebElement el;

        do {
            if(elapse != -1) {
                try {
                    Thread.sleep(pollMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            el = finder.find(locator, false);
            if(el == null) {
                throw new IllegalStateException(String.format("No element found with locator '%s'.", locator));
            }

            elapse = System.currentTimeMillis() - start;
        } while(!el.isDisplayed() && elapse < timeoutMillis);

        if(!el.isDisplayed()) {
            throw new IllegalStateException(String.format("timeout for locating '%s' (%d ms) reached.", locator, timeoutMillis));
        }
    }

    public void cssShouldMatchXTimes(String cssLocator, int count) {
        int actual = getMatchingCSSCount(cssLocator);
        if(actual != count) {
            throw new AssertionError(String.format("Matching css count for %s expected is %d, but was %d.", cssLocator, count, actual));
        }
    }

    public void xpathShouldMatchXTimes(String xpath, int count) {
        int actual = getMatchingXpathCount(xpath);
        if(actual != count) {
            throw new AssertionError(String.format("Matching xpath count for %s expected is %d, but was %d.", xpath, count, actual));
        }
    }

    private String closeAlert() {
        return closeAlert(false);
    }

    private String closeAlert(boolean confirm) {
        Alert alert = driver.switchTo().alert();
        String text = alert.getText();
        if (!confirm) {
            alert.dismiss();
        } else {
            alert.accept();
        }

        return text;
    }

    private File newScreenCaptureFile() {
        String name = String.format("screen_capture_%d_%d.png", screenCaptureSeed, ++screenCaptureCtr);

        return new File(screenCaptureDir, name);
    }

    private boolean frameContains(String locator, String text) {
        WebElement el = finder.find(locator);
        driver.switchTo().frame(el);
        LOG.info(String.format("Searching for text from frame '%s'.", locator));
        boolean found = textIsPresent(text);
        driver.switchTo().defaultContent();
        return found;
    }

    private WebElement getCheckbox(String locator){
        return finder.find(locator, "input");
    }

    private List<WebElement> getRadioButton(String groupName) {
        String xpath = String.format("//input[@type='radio' and @name='%s']", groupName);
        LOG.info("Radio group locator: " + xpath);

        return driver.findElements(By.xpath(xpath));
    }

    private String getValueFromRadioButtons(List<WebElement> els) {
        for (WebElement el : els) {
            if (el.isSelected()) {
                return el.getAttribute("value");
            }
        }

        return null;
    }

    private WebElement getRadioButtonWithValue(String groupName, String value) {
        String xpath = String.format("//input[@type='radio' and @name='%s' and (@value='%s' or @id='%s')]", groupName, value, value);
        LOG.info("Radio group locator: " + xpath);

        return ElementFinder.findByXpath(driver,xpath,null,null);
    }

    /*
    private WebElement getCheckboxWithValue(String groupName, String value) {
        String xpath = String.format("//input[@type='checkbox' and @name='%s' and (@value='%s' or @id='%s')]", groupName, value, value);
        LOG.info("Checkbox group locator: " + xpath);

        return ElementFinder.findByXpath(driver,xpath,null,null);
    }*/

    private boolean textIsPresent(String text) {
        String locator = String.format("xpath=//*[contains(., %s)]", escapeXpathValue(text));
        return finder.find(locator, false) != null;
    }

    private String escapeXpathValue(String value) {
        // "I'm here!"
        // -> concat('"I', "'", 'm here!"')
        // -> '"I' + "'" + 'm here!"'
        if (StringUtils.contains(value,'"') && StringUtils.contains(value,'\'')) {
            String [] parts_wo_apos = StringUtils.split(value,'\'');
            return String.format("concat('%s')", StringUtils.join(parts_wo_apos, "', \"'\", '"));
        }

        // I'm Lucky
        // -> "I'm Lucky"
        if(StringUtils.contains(value, "\'")) {
            return String.format("\"%s\"", value);
        }

        // Hi there
        // -> 'Hi There'
        return String.format("'%s'", value);
    }

    private void deleteCookie(Cookie cookie) {
        driver.manage().deleteCookie(cookie);
    }

    private boolean isEnabled(String locator) {
        WebElement el = finder.find(locator);

        if (!isFormElement(el)) {
            throw new AssertionError(String.format("Element %s is not an input.", locator));
        }

        if (!el.isEnabled()) {
            return false;
        }

        String readOnly = el.getAttribute("readonly");

        if (readOnly.equalsIgnoreCase("readonly") || readOnly.equalsIgnoreCase("true")) {
            return false;
        }

        return true;
    }

    private boolean isFormElement(WebElement element) {
        if (element == null) {
            return false;
        }

        String tagName = element.getTagName().toLowerCase();
        return (tagName.equalsIgnoreCase("input") || tagName.equalsIgnoreCase("select") || tagName.equalsIgnoreCase("textarea") || tagName.equalsIgnoreCase("button"));
    }

    private boolean isVisible(String locator) {
        WebElement el = finder.find(locator);

        if (el == null) {
            throw new AssertionError(String.format("Locator %s not found.", locator));
        }

        return el.isDisplayed();
    }

    private String[] parseAttributeLocator(String attributeLocator) {
        String[] parts = attributeLocator.split("@");
        LOG.info("parts:" + Arrays.asList(parts));
        LOG.info("size " + parts.length);
        if (parts.length == 0  || parts.length < 2) {
            throw new IllegalArgumentException(String.format("Invalid attribute locator '%s'", attributeLocator));
        }
        if (StringUtils.isEmpty(parts[0])) {
            throw new IllegalStateException(String.format("Attribute locator '%s' does not contain an element locator", attributeLocator));
        }

        if (StringUtils.isEmpty(parts[1])) {
            throw new IllegalStateException(String.format("Attribute locator '%s' does not contain an attribute name", attributeLocator));
        }

        return new String[] {parts[0],parts[1]};
    }

    public String getInnerHtml(String locator) {
        WebElement el = finder.find(locator);

        return (String) executor.executeScript("return arguments[0].innerHTML;", el);
    }

    public void focus(String locator) {
        WebElement el = finder.find(locator);

        executor.executeScript("arguments[0].focus();", el);
    }


    private String getText(String locator, boolean validateResult) {
        WebElement el = finder.find(locator,validateResult);
        if (el != null) {
            return el.getText();
        }
        return null;
    }

    private void inputTextIntoTextField(String locator, String text) {
        WebElement el = finder.find(locator);
        el.clear();
        el.sendKeys(text);
    }

    private boolean pageContains(String text) {
        driver.switchTo().defaultContent();

        if(textIsPresent(text)) {
            return true;
        }

        List<WebElement> frames = driver.findElements(By.xpath("//frame|//iframe"));

        for(WebElement frame : frames) {
            driver.switchTo().frame(frame);

            if(textIsPresent(text)) {
                return true;
            }

            driver.switchTo().defaultContent();
        }

        return false;
    }

    private boolean isElementPresent(String locator, String tagName, String attrName, String attrValue) {
        return finder.find(locator, false, tagName, attrName, attrValue) != null;
    }

    private boolean isElementPresent(String locator, String tagName) {
        return finder.find(locator, false, tagName) != null;
    }

    private boolean isElementPresent(String locator) {
        return finder.find(locator, false) != null;
    }

    private WebElement getSelectList(String locator) {
        return finder.find(locator, true, "select");
    }

    private List<WebElement> getSelectListOptions(String locator) {
        WebElement selectEl = getSelectList(locator);
        Select select = new Select(selectEl);

        return select.getOptions();
    }

    private List<WebElement> getSelectListOptions(WebElement selectEl) {
        Select select = new Select(selectEl);

        return select.getOptions();
    }

    private List<String> getLabelsForOptions(List<WebElement> selectOptions) {
        List<String> labels = new ArrayList<String>();
        for (WebElement option : selectOptions) {
            labels.add(option.getText());
        }
        return labels;
    }

    private List<WebElement> getSelectListOptionsSelected(String locator) {
        List<WebElement> selectOptions = getSelectListOptions(locator);

        List<WebElement> selectedOptions = new ArrayList<WebElement>();
        for (WebElement selected: selectOptions) {
            if (selected.isSelected()) {
                selectedOptions.add(selected);
            }
        }
        return selectedOptions;
    }

    private List<String> getValuesForOptions(List<WebElement> selectedOptions) {
        List<String> values = new ArrayList<String>();
        for (WebElement option : selectedOptions) {
            values.add(option.getAttribute("value"));
        }
        return values;
    }

    private boolean isMultiSelectList(WebElement el) {
        String multipleValue = el.getAttribute("multiple");

        if (multipleValue != null && (multipleValue.equalsIgnoreCase("true") || multipleValue.equalsIgnoreCase("multiple"))) {
            return true;
        }

        return false;
    }

    private String mapAsciiKeyCodeToKey (int keyCode) {
        Map<Integer, Keys> keysMap = new HashMap<Integer, Keys>();
        keysMap.put(0,Keys.NULL);
        keysMap.put(8,Keys.BACK_SPACE);
        keysMap.put(9,Keys.TAB);
        keysMap.put(10,Keys.RETURN);
        keysMap.put(13,Keys.ENTER);
        keysMap.put(24,Keys.CANCEL);
        keysMap.put(27,Keys.ESCAPE);
        keysMap.put(32,Keys.SPACE);
        keysMap.put(42,Keys.MULTIPLY);
        keysMap.put(43,Keys.ADD);
        keysMap.put(44,Keys.SUBTRACT);
        keysMap.put(56,Keys.DECIMAL);
        keysMap.put(57,Keys.DIVIDE);
        keysMap.put(59,Keys.SEMICOLON);
        keysMap.put(61,Keys.EQUALS);
        keysMap.put(127,Keys.DELETE);
        Keys key = keysMap.get(Integer.valueOf(keyCode));

        if (key == null) {
            Character c = (char) keyCode;
            return c.toString();
        }

        return key.toString();
    }

    public boolean hasElement(String locator) {
        return finder.find(locator, false) != null;
    }

}
