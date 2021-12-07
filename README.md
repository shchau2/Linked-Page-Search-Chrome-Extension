Video Link: https://youtu.be/sdzWz46usK0

Steps:
1. Python 3

2. pip install tornado bs4 requests

3. Java SE 17
https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

4. Test for Java setup (Optional):
java -jar main.jar

Expected output:
```
Usage:
For building index: -b [directory path]
For searching: -s [directory path] [query]
For topic analysis: -t [directory path] [number of topics] [probability of background]
```

5. python server.py

6. Use case: https://xuanji.appspot.com/isicp/index.html

7. Click "Build", wait until the output box show "Build: [num docs] files are added".

8. Enter query for search or enter parameters for topic analysis.

Notes:
1) If the search tab is idle for long time, Chrome may close the communication port between the tab and the background script, making the tab unresponsive to button clicks.
You can start a new tab to continue searching without building the index again if the index is already built (the program uses URL as identifier).

2) All scraped files and built indexes are stored in server/documents folder.
