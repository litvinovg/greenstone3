package gstests;

import java.util.List;

import org.greenstone.gsdl3.testing.GSTestingUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class TestClass
{
	//TODO: Do these dynamically
	private static final int NUMBER_OF_CLASSIFIERS = 4;
	private static final int NUMBER_OF_SEARCH_TYPES = 3;
	private static final int NUMBER_OF_SEARCH_INDEXES = 5;

	//TODO: Turn these into a list
	private static final int TITLE_CLASSIFIER_SIZE = 11;
	private static final int SUBJECT_CLASSIFIER_SIZE = 7;
	private static final int ORGANISATIONS_CLASSIFIER_SIZE = 5;

	private static final int HITS_PER_PAGE = 20;
	private static final int SNAILS_RESULT_COUNT = 58;
	private static final int SNAILS_OCCURENCE_COUNT = 398;

	private static final String COLLECTION_NAME = "Demo Collection";

	private static WebDriver _driver = new FirefoxDriver();

	@Before
	public void init()
	{
		_driver.get("http://localhost:8487/greenstone3/library");
	}

	@Test
	/**
	 * Test the library home page
	 * 1. Test that the demo collection is there
	 */
	public void testHomePage()
	{
		Assert.assertNotNull(GSTestingUtil.findElementByXPath(_driver, "//div[@id='collectionLinks']/a[@title='Demonstration collection containing a few books from the Humanitarian and Development Libraries. Uses Lucene and JDBM.']"));
	}

	@Test
	public void testDemoCollectionHomePage()
	{
		GSTestingUtil.loadCollectionByName(_driver, COLLECTION_NAME);

		//Check the title is correct
		WebElement demoTitleElem = GSTestingUtil.findElementByXPath(_driver, "//div[@id='titlearea']/h2");
		String title = demoTitleElem.getText();
		Assert.assertTrue(title.equals("Demo Collection"));

		//Check we have four browsing classifiers
		List<WebElement> classifierLinks = GSTestingUtil.findElementsByXPath(_driver, "//ul[@id='gs-nav']/li");
		Assert.assertEquals(classifierLinks.size(), NUMBER_OF_CLASSIFIERS);

		//Check we have 3 search types
		List<WebElement> searchTypes = GSTestingUtil.findElementsByXPath(_driver, "//div[@id='quicksearcharea']/ul/li");
		Assert.assertEquals(searchTypes.size(), NUMBER_OF_SEARCH_TYPES);

		//Check we have 5 search indexes
		List<WebElement> searchIndexes = GSTestingUtil.findElementsByXPath(_driver, "//div[@id='quicksearcharea']/form/span[@class='textselect']/select/option");
		Assert.assertEquals(searchIndexes.size(), NUMBER_OF_SEARCH_INDEXES);
	}

	@Test
	public void testTitleClassifier()
	{
		GSTestingUtil.loadCollectionByName(_driver, COLLECTION_NAME);

		//Load the title classifier
		GSTestingUtil.loadClassifierByName(_driver, "titles");

		//Check that we have 11 documents
		List<WebElement> documents = GSTestingUtil.findElementsByXPath(_driver, "//table[@id='classifiernodelist']/tbody/tr");
		Assert.assertEquals(documents.size(), TITLE_CLASSIFIER_SIZE);
	}

	@Test
	public void testSubjectsClassifier()
	{
		GSTestingUtil.loadCollectionByName(_driver, COLLECTION_NAME);

		//Load the subject classifier
		GSTestingUtil.loadClassifierByName(_driver, "subjects");

		//Check that we have 7 subjects
		List<WebElement> subjectElems = GSTestingUtil.findElementsByXPath(_driver, "//table[@id='classifiernodelist']/tbody/tr");
		Assert.assertEquals(subjectElems.size(), SUBJECT_CLASSIFIER_SIZE);

		//Get all of the subject expand images
		List<WebElement> expandImages = GSTestingUtil.findElementsByXPath(_driver, "//img[@src='interfaces/default/images/expand.png']");

		//Open up a random subject
		WebElement randomSubject = expandImages.get((int) (Math.random() * SUBJECT_CLASSIFIER_SIZE));
		randomSubject.click();

		//Make sure it opened correctly
		String sectionNumber = randomSubject.getAttribute("id").substring(6);
		GSTestingUtil.waitForXPath(_driver, "//table[@id='div" + sectionNumber + "']");
		Assert.assertNotNull(GSTestingUtil.findElementByXPath(_driver, "//table[@id='div" + sectionNumber + "']"));
	}

	@Test
	public void testOrganisationsClassifier()
	{
		GSTestingUtil.loadCollectionByName(_driver, COLLECTION_NAME);

		//Load the organisation classifier
		GSTestingUtil.loadClassifierByName(_driver, "organisations");

		//Check that we have 5 organisations
		List<WebElement> orgElems = GSTestingUtil.findElementsByXPath(_driver, "//table[@id='classifiernodelist']/tbody/tr");
		Assert.assertEquals(orgElems.size(), ORGANISATIONS_CLASSIFIER_SIZE);

		//Get all of the subject expand images
		List<WebElement> expandImages = GSTestingUtil.findElementsByXPath(_driver, "//img[@src='interfaces/default/images/expand.png']");

		//Open up a random organisation
		WebElement randomOrganisation = expandImages.get((int) (Math.random() * ORGANISATIONS_CLASSIFIER_SIZE));
		randomOrganisation.click();

		//Make sure it opened correctly
		String sectionNumber = randomOrganisation.getAttribute("id").substring(6);
		GSTestingUtil.waitForXPath(_driver, "//table[@id='div" + sectionNumber + "']");
		Assert.assertNotNull(GSTestingUtil.findElementByXPath(_driver, "//table[@id='div" + sectionNumber + "']"));
	}

	@Test
	public void testQuickSearch()
	{
		GSTestingUtil.loadCollectionByName(_driver, COLLECTION_NAME);

		/*
		 * TEST A QUERY THAT SHOULD WORK
		 */

		//Type "snails" into quick search area and submit 
		WebElement quickSearchInput = GSTestingUtil.findElementByXPath(_driver, "//div[@id='quicksearcharea']//input[@name='s1.query']");
		quickSearchInput.sendKeys("snails");
		WebElement quickSearchSubmitButton = GSTestingUtil.findElementByXPath(_driver, "//input[@id='quickSearchSubmitButton']");
		quickSearchSubmitButton.click();

		//Check the number of results on the page
		List<WebElement> results = GSTestingUtil.findElementsByXPath(_driver, "//table[@id='resultsTable']/tbody/tr");
		Assert.assertEquals(results.size(), HITS_PER_PAGE);

		//Check the term info has the correct values
		WebElement termInfo = GSTestingUtil.findElementByXPath(_driver, "//p[@class='termList']/span[@class='termInfo']");
		Assert.assertTrue(termInfo.getText().equals("snails occurs " + SNAILS_OCCURENCE_COUNT + " times in " + SNAILS_RESULT_COUNT + " sections"));

		//Check the search results status bar
		WebElement searchStatus = GSTestingUtil.findElementByXPath(_driver, "//td[@id='searchResultsStatusBar']");
		Assert.assertTrue(searchStatus.getText().equals("Displaying 1 to " + HITS_PER_PAGE + " of " + SNAILS_RESULT_COUNT + " sections"));

		//Click the next button
		WebElement nextButton = GSTestingUtil.findElementByXPath(_driver, "//td[@id='nextTD']/a");
		nextButton.click();

		//Check the search results status bar on the new page
		searchStatus = GSTestingUtil.findElementByXPath(_driver, "//td[@id='searchResultsStatusBar']");
		Assert.assertTrue(searchStatus.getText().equals("Displaying " + (HITS_PER_PAGE + 1) + " to " + (HITS_PER_PAGE * 2) + " of " + SNAILS_RESULT_COUNT + " sections"));

		//Click the previous button
		WebElement prevButton = GSTestingUtil.findElementByXPath(_driver, "//td[@id='prevTD']/a");
		prevButton.click();

		/*
		 * TEST A RANDOM QUERY THAT SHOULD FAIL
		 */

		//Generate a search that will fail
		quickSearchInput = GSTestingUtil.findElementByXPath(_driver, "//div[@id='quicksearcharea']//input[@name='s1.query']");
		quickSearchInput.clear();
		quickSearchInput.sendKeys(GSTestingUtil.generateRandomString(20));
		quickSearchSubmitButton = GSTestingUtil.findElementByXPath(_driver, "//input[@id='quickSearchSubmitButton']");
		quickSearchSubmitButton.click();

		//Make sure that no documents match
		WebElement contentElem = GSTestingUtil.findElementByXPath(_driver, "//div[@id='gs_content']");
		Assert.assertTrue(contentElem.getText().equals("No documents matched the query."));
	}

	@Test
	public void testLoginAndAdmin()
	{
		GSTestingUtil.loginAs(_driver, "admin", "admin");

		//Go to the admin page
		WebElement adminPagesLink = GSTestingUtil.findElementByXPath(_driver, "//a[@href='library/admin/ListUsers']");
		adminPagesLink.click();

		//Make sure we are logged in correctly
		WebElement userListTable = GSTestingUtil.findElementByXPath(_driver, "//table[@id='userListTable']");
		Assert.assertNotNull(userListTable);

		//Go to the add new user page
		WebElement addNewUserButton = GSTestingUtil.findElementByXPath(_driver, "//a[@href='library/admin/AddUser']");
		addNewUserButton.click();

		//Get the form elements 
		WebElement usernameBox = GSTestingUtil.findElementByXPath(_driver, "//input[@name='s1.username']");
		WebElement passwordBox = GSTestingUtil.findElementByXPath(_driver, "//input[@id='passwordOne']");
		WebElement repasswordBox = GSTestingUtil.findElementByXPath(_driver, "//input[@id='passwordTwo']");
		WebElement emailBox = GSTestingUtil.findElementByXPath(_driver, "//input[@name='s1.email']");
		WebElement groupsBox = GSTestingUtil.findElementByXPath(_driver, "//input[@name='s1.groups']");
		WebElement commentBox = GSTestingUtil.findElementByXPath(_driver, "//textarea[@name='s1.comment']");

		//Generate a random user name of password
		String randomUsername = GSTestingUtil.generateRandomString(8);
		String randomPassword = GSTestingUtil.generateRandomString(8);

		//Enter information into the form
		usernameBox.sendKeys(randomUsername);
		passwordBox.sendKeys(randomPassword);
		repasswordBox.sendKeys(randomPassword);
		emailBox.sendKeys(randomUsername + "@testusername.co.nz");
		groupsBox.sendKeys("Test Group");
		commentBox.sendKeys("A user added for testing purposes");

		//Submit the form
		WebElement submitButton = GSTestingUtil.findElementByXPath(_driver, "//input[@id='submitButton']");
		submitButton.click();

		//Check the new user information is correct
		List<WebElement> userRows = GSTestingUtil.findElementsByXPath(_driver, "//table[@id='userListTable']/tbody/tr");
		boolean found = false;
		for (int i = 0; i < userRows.size(); i++)
		{
			List<WebElement> columns = userRows.get(i).findElements(By.tagName("td"));

			if (columns.get(0).getText().equals(randomUsername))
			{
				found = true;
				Assert.assertTrue(columns.get(1).getText().equals("enabled"));
				Assert.assertTrue(columns.get(2).getText().equals("TestGroup"));
				Assert.assertTrue(columns.get(3).getText().equals("A user added for testing purposes"));
				Assert.assertTrue(columns.get(4).getText().equals(randomUsername + "@testusername.co.nz"));
			}
		}
		Assert.assertTrue(found);

		//Log in as the new user
		GSTestingUtil.logout(_driver);
		GSTestingUtil.loginAs(_driver, randomUsername, randomPassword);

		//Check the log in worked
		WebElement menuButton = GSTestingUtil.findElementByXPath(_driver, "//li[@id='userMenuButton']/a");
		Assert.assertTrue(menuButton.getText().equals(randomUsername));

		//Go to the home page
		WebElement backToHomePageLink = GSTestingUtil.findElementByXPath(_driver, "//div[@id='breadcrumbs']/a[1]");
		backToHomePageLink.click();

		//Log in as admin
		GSTestingUtil.logout(_driver);
		GSTestingUtil.loginAs(_driver, "admin", "admin");

		//Go to the list of users
		addNewUserButton = GSTestingUtil.findElementByXPath(_driver, "//a[@href='library/admin/ListUsers']");
		addNewUserButton.click();

		//Delete the user
		userRows = GSTestingUtil.findElementsByXPath(_driver, "//table[@id='userListTable']/tbody/tr");
		for (int i = 0; i < userRows.size(); i++)
		{
			List<WebElement> columns = userRows.get(i).findElements(By.tagName("td"));

			if (columns.get(0).getText().equals(randomUsername))
			{
				WebElement deleteLink = columns.get(6).findElement(By.xpath(".//input[@value='Delete']"));
				deleteLink.click();
				_driver.switchTo().alert().accept();

				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		//Log out as admin
		GSTestingUtil.logout(_driver);
	}

	@Test
	public void testDocumentView()
	{
		GSTestingUtil.loadCollectionByName(_driver, COLLECTION_NAME);
		GSTestingUtil.loadClassifierByName(_driver, "titles");

		//Load a random document
		List<WebElement> documents = GSTestingUtil.findElementsByXPath(_driver, "//table[@id='classifiernodelist']/tbody/tr");
		WebElement randomRow = documents.get((int) (Math.random() * documents.size()));
		WebElement link = randomRow.findElement(By.xpath(".//a"));
		link.click();

		//Check the cover image is loaded
		WebElement coverImage = GSTestingUtil.findElementByXPath(_driver, "//div[@id='coverImage']/img");
		Assert.assertTrue(GSTestingUtil.isImageLoaded(_driver, coverImage));
	}

	@AfterClass
	public static void destroy()
	{
		_driver.quit();
	}
}