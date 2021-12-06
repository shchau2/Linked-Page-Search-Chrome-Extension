import tornado.ioloop
import tornado.web
import tornado.websocket

import json

import scraper
import search_engine

class Server(tornado.websocket.WebSocketHandler):
    def check_origin(self, origin):
        return True
    
    def open(self):
        print("WebSocket opened.")
        
    def on_message(self, message):
        obj = json.loads(message)
        print(obj)
        if "build" in obj:
            self.handle_build(obj["url"])
        elif "query" in obj:
            self.handle_query(obj["url"], obj["query"])
        elif "num_topics" in obj:
            self.handle_topic(obj["url"], obj["num_topics"], obj["prob_background"])
        self.on_close()
        self.close()

    def on_close(self):
        print("WebSocket closed.")
    
    def handle_build(self, url):
        self.write_message("Scraping...")
        count = scraper.scrape(url)
        if count == -1:
            self.write_message("Error: scraping failed")
            return
        else:
            self.write_message("Success: scraped " + str(count) + " pages<br>Building index...")
        se = search_engine.SearchEngine(url)
        self.write_message(se.build())
        
    def handle_query(self, url, query):
        self.write_message("Searching...")
        se = search_engine.SearchEngine(url)
        self.write_message(se.search(query))
        
    def handle_topic(self, url, num_topics, prob_background):
        self.write_message("Analysing topics...")
        se = search_engine.SearchEngine(url)
        self.write_message(se.plsa(num_topics, prob_background))

def make_app():
    return tornado.web.Application([
        (r"/ws", Server),
    ])

if __name__ == "__main__":
    app = make_app()
    app.listen(8888)
    tornado.ioloop.IOLoop.current().start()