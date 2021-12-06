from bs4 import BeautifulSoup
import os
import requests

import file_manager
    
def get_base_url(url):
    components = parse_url(url)
    if len(components) < 3:
        return "://".join(components)
    else:
        return components[0] + "://" + "/".join(components[1:-1])

def is_absolute_path(path):
    if path[0:7] == "http://" or path[0:8] == "https://":
        return True
    else:
        return False

def parse_url(url):
    return url.replace("://", "/").split("/")

def scrape(url):
    if not is_absolute_path(url):
        print("Error: Invalid URL")
        return -1
    fm = file_manager.FileManager(url)
    fm.delete_all()
        
    response = requests.get(url)
    if response.status_code != requests.codes.ok:
        print("Error: Cannot request page", url)
        return -1
    soup = BeautifulSoup(response.text, "html.parser")
    base_url = get_base_url(url)
    
    count = 0
    for tag in soup.find_all("a", href=True):
        link = ""
        if is_absolute_path(tag["href"]):
            link = tag["href"]
        else:
            link = base_url + "/" + tag["href"]
        
        r = requests.get(link)    
        if r.status_code != requests.codes.ok:
            print("Error: cannot requsest page", link)
            continue
        s = BeautifulSoup(r.text, "html.parser")
        
        count += fm.save(count, link, tag.text, s.get_text())
    
    return count