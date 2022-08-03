# -*- coding: utf-8 -*-
"""
Created on Thu Jan 28 00:44:25 2021

@author: chakati
"""
from typing import Dict
import cv2
import numpy as np
import os
import tensorflow as tf
import csv

## import the handfeature extractor class
from frameextractor import frameExtractor
from handshape_feature_extractor import HandShapeFeatureExtractor as HSFE

__extractor = HSFE.get_instance()

def train_frameExtractor(videopath, frames_path):
    if not os.path.exists(frames_path):
        os.mkdir(frames_path)
    cap = cv2.VideoCapture(videopath)
    video_length = int(cap.get(cv2.CAP_PROP_FRAME_COUNT)) - 1
    frame_no= int(video_length/2)
    cap.set(1,frame_no)
    ret,frame=cap.read()
    video_name = videopath.split(os.path.sep)[-1]
    # gesture_name = get_gesture_name(video_name)
    cv2.imwrite(frames_path + "/{0}.png".format(video_name.split(".")[0]), frame)

def get_gesture_name(video_name):
    split_extension = video_name.split(".")[0]
    split_dash = split_extension.split("-")
    return split_dash[0] + split_dash[-1]

def extract_frames(videos_path, frames_path, count, train_mode = False):
    for video_name in os.listdir(videos_path):
        if video_name.startswith('.'):
            continue
        video_ab_path = os.path.join(videos_path, video_name)
        if train_mode :
            train_frameExtractor(video_ab_path, frames_path)
        else: 
            frameExtractor(video_ab_path, frames_path, count)
        count+=1

def get_train_feature_vector(frames_path):
    feature_dict = {}
    for image in os.listdir(frames_path):
        if image.startswith('.'):
            continue
        image_path = os.path.join(frames_path, image)
        im_data = cv2.imread(image_path,cv2.IMREAD_GRAYSCALE)
        im_feature = __extractor.extract_feature(im_data)
        im_name = image.split(".")[0].split("_")[0]
        if im_name in feature_dict.keys():
            feature_dict[im_name].append(im_feature)
        else:
            feature_dict[im_name] = [im_feature]
    return feature_dict

def get_test_feature_vector(frames_path):
    feature_list = []
    for image in os.listdir(frames_path):
        if image.startswith('.'):
            continue
        image_path = os.path.join(frames_path, image)
        im_data = cv2.imread(image_path,cv2.IMREAD_GRAYSCALE)
        im_feature = __extractor.extract_feature(im_data)
        feature_list.append(im_feature)
    return feature_list

name_to_label = {'Num0':0, 'Num1':1, 'Num2':2, 'Num3':3, 'Num4':4, 'Num5':5, 'Num6':6, 'Num7':7, 'Num8':8, 'Num9':9, 'FanDown':10, 'FanOn':11, 'FanOff':12, 'FanUp':13, 'LightOff':14, 'LightOn':15, 'SetThermo':16}
test_to_practice = {'0':'Num0', '1':'Num1', '2':'Num2', '3':'Num3', '4':'Num4', '5':'Num5', '6':'Num6', '7':'Num7', '8':'Num8', '9':'Num9', 'DecreaseFanSpeed':'FanDown', 'FanOn':'FanOn', 'FanOff':'FanOff', 'IncreaseFanSpeed':'FanUp', 'LightOff':'LightOff', 'LightOn':'LightOn', 'SetThermo':'SetThermo'}
    
# =============================================================================
# Get the penultimate layer for trainig data
# =============================================================================
# your code goes here
# Extract the middle frame of each gesture video
traindata_path = os.path.join(os.getcwd(),'traindata')
traindata_frames = os.path.join(os.getcwd(), 'train_frames')

extract_frames(traindata_path, traindata_frames, 0, True)
train_feature_dict = get_train_feature_vector(traindata_frames)

# =============================================================================
# Get the penultimate layer for test data
# =============================================================================
# your code goes here 
# Extract the middle frame of each gesture video
testdata_path = os.path.join(os.getcwd(),'test')
testdata_frames = os.path.join(os.getcwd(), 'test_frames')

# ToDo: This is for verifying, need to be deleted
extract_frames(testdata_path, testdata_frames, 0)
test_feature_list = get_test_feature_vector(testdata_frames)

# =============================================================================
# Recognize the gesture (use cosine similarity for comparing the vectors)
# =============================================================================
cos_sim = tf.keras.losses.cosine_similarity
def find_gesture():
    sim_results = []
    for test_fv in test_feature_list:
        gesture_sim_values = [0] * 17
        for gesture in train_feature_dict.keys():
            gesture_name = gesture.split('-')[0]
            ges_label = name_to_label[gesture_name]
            train_feature_fvs = train_feature_dict[gesture]
            sim = comparing(test_fv, train_feature_fvs)
            gesture_sim_values[ges_label] = sim
        max_sim = min(gesture_sim_values)
        max_sim_label = gesture_sim_values.index(max_sim)
        sim_results.append(max_sim_label)
    # need to change
    # gesture_dict['frame_name'] = max_sim_label
    return sim_results


def comparing(train_gesture_fvs, test_fv):
    group_sim = cos_sim(test_fv, train_gesture_fvs, axis=-1)
    greater_sim = min(group_sim.numpy())
    return greater_sim

def accuracy(test_result):
    result, count = [], 0
    for name in os.listdir(os.path.join(os.getcwd(), "test")):
        if name.startswith('.'):
            continue
        gesture = name.split('.')[0].split('-')[-1]
        practice_name = test_to_practice[gesture]
        label = name_to_label[practice_name]
        result.append(label)
    for i in range(len(test_result)):
        if int(test_result[i]) == result[i]:
            count = count + 1
    return float(count) / len(test_result)

def main():
    sim_results = find_gesture()
    print("Accuracy is: " + str(accuracy(sim_results)))
    with open(os.path.join(os.getcwd(),'result.csv'), 'w', encoding='UTF8') as result:
        writer = csv.writer(result)
        writer.writerow(sim_results)

if __name__ == "__main__":
    main()