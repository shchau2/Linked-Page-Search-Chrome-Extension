import os
import requests
from bs4 import BeautifulSoup

def clean_file_name(raw):
    return "".join(x for x in raw if x.isalnum() or x in " _-")

def clean_text(raw):
    paragraphs = raw.split("\n")
    cleaned = filter(lambda x : len(x) > 2, paragraphs)
    return "\n".join(cleaned)
    
def get_base_url(url):
    split_1 = url.split("//")
    split_2 = split_1[-1].split("/")
    
    prefix = ""
    if len(split_1) > 1:
        prefix = split_1[0] + "//"
    if len(split_2) > 1:
        return prefix + "/".join(split_2[:-1])
    else:
        return prefix + "/".join(split_2)

def is_absolute_path(path):
    if path[0:7] == "http://" or path[0:8] == "https://":
        return True
    else:
        return False

def scrape(url):
    response = requests.get(url)
    if response.status_code != requests.codes.ok:
        print("Error: cannot request page", url)
        return
    soup = BeautifulSoup(response.text, "html.parser")
    
    base_url = get_base_url(url)
    directory_name = clean_file_name(url)
    os.mkdir("documents/{dir}".format(dir=directory_name))
    os.mkdir("documents/{dir}/contents".format(dir=directory_name))
    
    count = 0
    for tag in soup.find_all("a", href=True):
        link = ""
        if is_absolute_path(tag["href"]):
            link = tag["href"]
        else:
            link = base_url + "/" + tag["href"]
        
        link_response = requests.get(link)    
        if link_response.status_code != requests.codes.ok:
            print("Error: cannot requsest page", link)
            continue
        link_soup = BeautifulSoup(link_response.text, "html.parser")
        file_name = clean_file_name(link_soup.title.get_text())
        file_content = clean_text(link_soup.get_text())
        
        try:
            with open("documents/{dir}/contents/{file}".format(dir=directory_name, file=file_name), "w", encoding="utf-8") as f:
                f.write(file_content)
            count += 1
        except:
            print("Error: cannot save page", link)
            
    print(count, "pages saved.")
    
if __name__ == "__main__":
    scrape("https://xuanji.appspot.com/isicp/index.html")