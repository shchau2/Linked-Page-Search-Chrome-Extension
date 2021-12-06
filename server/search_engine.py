from bs4 import BeautifulSoup
import subprocess

import file_manager

class SearchEngine:
    def __init__(self, url):
        self.fm = file_manager.FileManager(url)
        self.dir = self.fm.get_dir()
        
    def build(self):
        process = subprocess.Popen(['java', '-jar', 'main.jar', '-b', self.dir], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = process.communicate()
        return out.decode("ascii", errors="ignore")
    
    def search(self, query):
        process = subprocess.Popen(['java', '-jar', 'main.jar', '-s', self.dir, query], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = process.communicate()
        soup = BeautifulSoup(out.decode("ascii", errors="ignore"), "html.parser")
        
        for div in soup.find_all("div"):
            filename = div["id"]
            url = self.fm.get_url_by_filename(filename)
            p_url = soup.new_tag("a", href=url)
            p_url.string = self.remove_id(filename, url)
            div.insert(0, p_url)
        return(str(soup))
        
    def plsa(self, k, b):
        process = subprocess.Popen(['java', '-jar', 'main.jar', '-t', self.dir, str(k), str(b)], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = process.communicate()
        soup = BeautifulSoup(out.decode("ascii", errors="ignore"), "html.parser")
        
        filename_to_div = {}
        for div in soup.find_all("div"):
            filename = div["id"]
            url = self.fm.get_url_by_filename(filename)
            p_url = soup.new_tag("a", href=url)
            p_url.string = self.remove_id(filename, url)
            div.insert(0, p_url)
            filename_to_div[filename] = str(div)
            
        html = str(soup.find("p"))
        for i in range(self.fm.count()):
            html += filename_to_div[self.fm.get_filename_by_id(i)]
            
        return html
    
    def remove_id(self, filename, default):
        if len(filename.split("_")) > 1:
            return filename.split("_")[1]
        else:
            return default