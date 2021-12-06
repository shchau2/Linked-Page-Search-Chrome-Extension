import os
import uuid

class FileManager:
    def __init__(self, url):
        uuids = {}
        with open("documents/.meta", "a+") as f:
            f.seek(0)
            
            for line in f.read().splitlines():
                _url, _uuid = line.split(",")
                uuids[_url] = _uuid
                
            if url not in uuids:
                _uuid = uuid.uuid4().hex
                f.write(url + "," + _uuid + "\n")
                uuids[url] = _uuid
        
        self.dir = "documents/" + uuids[url] + "/"
        self.cdir = self.dir + "contents/"
        self.toc_path = self.dir + ".toc"
        self.toc = None
        self.tocid = None
        
        if not os.path.isdir(self.dir):
            self.__mkdir()
    
    def __mkdir(self):
        os.mkdir(self.dir)
        os.mkdir(self.cdir)
        open(self.toc_path, "w").close()
        
    def __build_toc(self):
        if self.toc == None:
            self.toc = {}
            self.tocid = []
            with open(self.toc_path, "r") as f:
                for line in f.read().splitlines():
                    _id, _url, _path = line.split(",")
                    _id = int(_id)
                    _filename = _path.split("/")[-1]
                    self.toc[_filename] = _url
                    self.tocid.append([_filename, _url])
            
    
    def save(self, doc_id, url, link_text, text):
        try:
            path = self.link_text_to_path(doc_id, link_text)
            with open(path, "w", encoding="utf-8") as f:
                f.write(self.process_text(text))
            with open(self.toc_path, "a") as f:
                f.write(str(doc_id) + "," + url + "," + path + "\n")
            return 1
        except:
            print("Error: cannot save page", url)
            return 0      

    def delete_all(self):
        for f in os.listdir(self.cdir):
            os.remove(os.path.join(self.cdir, f))
        open(self.toc_path, "w").close()
            
    def link_text_to_path(self, doc_id, link_text):
        clean = "".join(x for x in link_text if x.isalnum() or x in "._-")
        if clean == "":
            return self.cdir + str(doc_id)
        else:
            return self.cdir + str(doc_id) + "_" + clean
    
    def process_text(self, text):
        ps = text.split("\n")
        return "\n".join(filter(lambda x : len(x) > 2, ps))
    
    def get_url_by_filename(self, filename):
        self.__build_toc()
        return self.toc[filename]
    
    def get_filename_by_id(self, id):
        self.__build_toc()
        return self.tocid[id][0]
        
    def count(self):
        self.__build_toc()
        return len(self.toc)
    
    def get_dir(self):
        return self.dir