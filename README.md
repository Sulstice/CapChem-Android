# CapChem

  **Introduction**
    
  CapChem is a way to recognize IUPAC and common molecule names and grabbing information such as 2D and 3D structure, NMR, IR, Molecular Weight, Boiling Point etc. from PubMed Database on the android platform.
    
  **How it Works**
    
  Capchem is simple with a lot of back-end being developed
    
  User Snaps a picture -> OCR Engine Analyzes Picture to get Text -> PubMed retrieves data result -> User is displayed information
    
  Capchem is used to make a life of a scientist easier when he/she is having trouble remembering the molecules and has to go through the constant struggle of looking up information. It also allows easier access for NMRs and IRs for data analysis of their molecules. Scientists can also rapidly discover what their molecule looks like without the fustration of reading IUPAC common names. 
    
  Non-scientists can also use this app based on user interest for information :). 
    
  **Development**
    
  UI was designed in Axure(7.0) by myself and is a rough outlook of what the app will look like.
    
  The App is still being developed and not released on Google Play. There is no future promise for IOS Development yet. Features included are:
    
- [x] User can Log in and log out of Parse Database system. The Data has also been transferred to MongoDB in the meantime. 
- [x] User can grab an image from their folder or use the camera to take a picture of an IUPAC name and run the OCR Engine on the image.
- [x] User has the ability to run the engine task Asynchronously or Synchronously
- [ ] A lot of the Front-End and UI Design still needs to be developed. 
- [ ] HTTP GET and POST requests for PubMed still need to be developed. 
- [ ] Recognition of molecules based on their skeleton structure.

### UI Design

**Log In Screen & Information Screen**

<p align="center">
  <img src="https://github.com/Sulstice/CapChem-Android/blob/master/UIDesign/login_page.png" width="350"/>
  <img src="https://github.com/Sulstice/CapChem-Android/blob/master/UIDesign/information_screen.png" width="350"/>
</p>

**Profile Page**

<p align="center">
  <img src="https://github.com/Sulstice/CapChem-Android/blob/master/UIDesign/profile_screen.png" width="350"/>
</p>

**App Landing Page**

http://sulstice.github.io/ 

**Acknoledgements**
Sam Dunning: Idea of using IUPAC Name instead of Skeleton Based Molecules
Icon Credit: 

<div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
