# ASU-Projects
##Course Projects

###For Part 1, this is a mobile application with the listed functionalities:###
```
    A. The user is shown a video of a gesture.
    B. The user can replay the video at least 3 times.
    C. Upon clicking the “PRACTICE” button, the user can capture his or her own video through the
smartphone’s front camera for a period of at most 5 seconds.
    D. The videos are uploaded to a server.
```
###The mobile application should have three (3) screens:###
```
    1. Screen 1: A drop-down menu of 17 different gestures will be shown on this screen. Once a single gesture is selected, the user will be taken to Screen 2.
a. Gesture list: {Turn on lights, Turn off lights, Turn on fan, Turn off fan, Increase fan speed,
decrease fan speed, Set Thermostat to specified temperature, gestures one for each digit
0,1,2,3,4,5,6,7,8,9}

    2. Screen 2: The video of an expert performing the gesture will be shown on this screen. Screen 2
will have another button that says “PRACTICE”. Once this button is pressed, the user will be
taken to Screen 3.

   3. Screen 3: In this screen, the camera interface will be opened for the user to record the practice
gesture. The video will be captured for 5 seconds, and the video will be saved with the following
filename format:
    ● [GESTURE NAME]_PRACTICE_[practice number].mp4
    
```

###For Part2###
###Task 1: Generate the penultimate layer for the training videos.###
```
Steps to generate the penultimate layer for the training set:
1. Extract the middle frames of all the training gesture videos.
2. For each gesture video, you will have one frame extract the hand shape feature by calling the
get_Intsance() method of the HandShapeFeatureExtractor class. (HandShapeFeatureExtractor
class uses CNN model that is trained for alphabet gestures)
3. For each gesture, extract the feature vector.
4. Feature vectors of all the gestures is the penultimate layer of the training set.
```
###Task 2: Generate the penultimate layer for the test videos###
```
Follow the steps for Task 1 to get the penultimate layer of the test dataset.
```
###Task 3: Gesture recognition of the test dataset.###
```
Steps:
1. Apply cosine similarity between the vector of the gesture video and the penultimate layer of the
training set. Corresponding gesture of the training set vector with minimum cosine difference is the
recognition of the gesture.
2. Save the gesture number to the results.csv
3. Recognize the gestures for all the test dataset videos and save the results to the results.csv file.
```
