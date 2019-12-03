import glob,os,re
from sys import argv
import pandas as pd
import numpy as np
import math

def search_files(pathwork):
    fileset = []
    # pathwork = '/home/gui/Documents/OpenAPS/openaps_monitor/Reseult/simulationCollectdata_Random_trasiant/'#os.getcwd()
    for root, dirs, files in os.walk(pathwork):
        for file in files:
            if file.endswith(".csv"):
                # filetype = file.replace('\n','')
                # filetype = filetype.split('.')
                # filetype = filetype[len(filetype)-1]  

                fileresult = os.path.join(root, file)
                fileset.append(fileresult)
                print(root,file)
    return fileset

def load_data_set(filelist,windows):
    # your code
    fp = open("./features_%sms.csv"%windows,'w')
    summLine = "mean_x,std_x,mean_y,std_y,mean_z,std_z,Activity\n"
    fp.write(summLine)  

    fp_12features = open("./features_12_%sms.csv"%windows,'w')
    summLine = "mean_x,std_x,median_x,rms_x,mean_y,std_y,median_y,rms_y,mean_z,std_z,median_z,rms_z,Activity\n"
    fp_12features.write(summLine)  


    for file in filelist:

        if "nothand_wash" in file:
            Activity = "no_hand_wash"
        else:
            Activity = "hand_wash"

        data = pd.read_csv(file,header=None)
        data.columns = ["time","x", "y", "z"]

        length = len(data.index)
        start_time = {'index':0, 'time': data["time"][0]} #record the start time
        i=0
        while (i<length):
            if data["time"][i]-start_time["time"] < windows:#*1000:
                i += 10 #update by 10ms to decrease time cost
            else: #windows > 1000 ms
                while(data["time"][i]-start_time["time"] >= windows):#1 second windows = 1000 ms
                    i -= 1
                i+=1 #update i by one unit
                mean_x = np.mean(data["x"][start_time["index"]:i])
                std_x = np.std(data["x"][start_time["index"]:i])
                mean_y = np.mean(data["y"][start_time["index"]:i])
                std_y = np.std(data["y"][start_time["index"]:i])
                mean_z = np.mean(data["z"][start_time["index"]:i])
                std_z = np.std(data["z"][start_time["index"]:i])

                median_x = np.median(data["x"][start_time["index"]:i])
                rms_x = math.sqrt(np.square(data["x"][start_time["index"]:i]).mean())
                median_y = np.median(data["y"][start_time["index"]:i])
                rms_y = math.sqrt(np.square(data["y"][start_time["index"]:i]).mean())
                median_z = np.median(data["z"][start_time["index"]:i])
                rms_z = math.sqrt(np.square(data["z"][start_time["index"]:i]).mean())


                summLine = "%s,%s,%s,%s,%s,%s,%s\n"%(mean_x,std_x,mean_y,std_y,mean_z,std_z,Activity)
                fp.write(summLine)  
                summLine = "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n"%(mean_x,std_x,median_x,rms_x,mean_y,std_y,median_y,rms_y,mean_z,std_z,median_z,rms_z,Activity)
                fp_12features.write(summLine)  

                start_time = {'index':i, 'time': data["time"][i]} #record the new start time

        #==========process the left less then 10 ms data========================================# 
        i -= 10 #backward 10 ms
        while (i<length):
            if data["time"][i]-start_time["time"] < windows:
                i += 1
            else: #windows > 1000 ms
                mean_x = np.mean(data["x"][start_time["index"]:i])
                std_x = np.std(data["x"][start_time["index"]:i])
                mean_y = np.mean(data["y"][start_time["index"]:i])
                std_y = np.std(data["y"][start_time["index"]:i])
                mean_z = np.mean(data["z"][start_time["index"]:i])
                std_z = np.std(data["z"][start_time["index"]:i])

                summLine = "%s,%s,%s,%s,%s,%s,%s\n"%(mean_x,std_x,mean_y,std_y,mean_z,std_z,Activity)
                fp.write(summLine)
                summLine = "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n"%(mean_x,std_x,median_x,rms_x,mean_y,std_y,median_y,rms_y,mean_z,std_z,median_z,rms_z,Activity)
                fp_12features.write(summLine)  
                break #stop when windows >1s as there is only 10 ms data left

    fp.close()

if __name__ == "__main__":
    #useage: python3 featureextraction.py path_to_raw_csv_data windows_slice(ms)
    fileset = search_files(argv[1])
    load_data_set(fileset,int(argv[2]))