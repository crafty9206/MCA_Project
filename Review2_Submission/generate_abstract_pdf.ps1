$ErrorActionPreference = 'Stop'
$out = 'C:\project\MCA_Project\Review2_Submission\R2_BATCH_MCA_9224116032_Deepak_Kumar_Abstract.pdf'
$editable = 'C:\project\MCA_Project\Review2_Submission\R2_BATCH_MCA_9224116032_Deepak_Kumar_Abstract_Source.pptx'
function rgb([string]$hex) {
    $h=$hex.TrimStart('#')
    return ([Convert]::ToInt32($h.Substring(0,2),16)+([Convert]::ToInt32($h.Substring(2,2),16)-shl 8)+([Convert]::ToInt32($h.Substring(4,2),16)-shl 16))
}
function box($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$fill,[int]$kind=1,[string]$line='') {
    $shape=$slide.Shapes.AddShape($kind,$x,$y,$w,$h)
    $shape.Fill.ForeColor.RGB=rgb $fill
    if($line){$shape.Line.ForeColor.RGB=rgb $line}else{$shape.Line.Visible=0}
    return $shape
}
function text($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$value,[double]$size=11,[string]$color='#203A3A',[bool]$bold=$false,[string]$font='Aptos') {
    $shape=$slide.Shapes.AddTextbox(1,$x,$y,$w,$h)
    $shape.TextFrame.WordWrap=-1
    $shape.TextFrame.MarginLeft=0;$shape.TextFrame.MarginRight=0;$shape.TextFrame.MarginTop=0;$shape.TextFrame.MarginBottom=0
    $range=$shape.TextFrame.TextRange
    $range.Text=$value.Replace('\n',[Environment]::NewLine)
    $range.Font.Name=$font;$range.Font.Size=$size;$range.Font.Bold=$bold;$range.Font.Color.RGB=rgb $color
    return $shape
}
$app=New-Object -ComObject PowerPoint.Application
$app.Visible=-1
$doc=$app.Presentations.Add()
$doc.PageSetup.SlideSize=7
$doc.PageSetup.SlideWidth=595
$doc.PageSetup.SlideHeight=842
$dark='#173B38';$teal='#28796F';$ink='#203A3A';$muted='#526965';$coral='#D88968'

# Page 1: Abstract and implementation context.
$s=$doc.Slides.Add(1,12);$s.Background.Fill.ForeColor.RGB=rgb '#FFFFFF'
box $s 0 0 12 842 $teal|Out-Null
text $s 48 38 380 28 'MAATRICARE' 22 $dark $true|Out-Null
text $s 48 70 470 35 'Pregnancy Support and Care-Coordination Application' 14 $ink $true|Out-Null
box $s 444 39 104 56 '#F2F7F4' 5 '#B9CEC5'|Out-Null
text $s 453 50 86 34 'COLLEGE LOGO\nPLACEHOLDER' 8 $muted $true|Out-Null
text $s 48 117 500 46 'Student: Deepak Kumar  |  Register No.: 9224116032\nDepartment: Computer Applications  |  Guide: [FILL]' 9 $muted|Out-Null
text $s 48 163 500 22 'Semester: [FILL]  |  Batch: [FILL]  |  KARE / CDOE' 9 $muted|Out-Null
box $s 48 197 500 1 '#D7E3DE'|Out-Null
text $s 48 214 500 26 'ABSTRACT' 14 $teal $true|Out-Null
$abstract='Pregnancy involves appointments, personal records, daily routines, and ongoing conversations with healthcare professionals. When details are scattered across calendars, notes, and memory, users may find it difficult to keep track of care-related activities or prepare questions for a visit. MaatriCare is a web-based pregnancy support and care-coordination application designed to bring these organizational activities together in one authenticated workspace.\n\nThe application provides pregnancy profile and progress information, appointment management, visit-question preparation, daily care tracking, a private symptom journal, pregnancy milestones, educational guides and resources, healthcare summaries, emergency contacts, and reminder preferences. Users can select recorded information for a report and control whether a report is shared through a time-limited link. The interface supports recording and reviewing information; it does not diagnose medical conditions, recommend treatment, or replace professional or emergency care.\n\nMaatriCare is implemented as a React and TypeScript frontend using Vite, with a Java Spring Boot REST backend. The backend uses Spring Security and JWT authentication, request validation, Spring Data JPA, and relational database support. MySQL is used for local development; PostgreSQL is configured for the production-oriented profile. Flyway migrations define the schema, while focused H2-backed tests verify selected backend behavior. User-owned records include pregnancy profiles, appointments, tasks, wellbeing records, symptoms, milestones, visit questions and answers, report shares, emergency contacts, and scheduled push reminders.'
text $s 48 248 500 435 $abstract 10.2 $ink|Out-Null
text $s 48 735 500 38 'Keywords: pregnancy support; care coordination; appointment management; private journal; Web Push; React; Spring Boot' 9 $teal $true|Out-Null
box $s 48 790 500 1 '#D7E3DE'|Out-Null;text $s 48 801 400 18 'Review 2 Abstract | MaatriCare' 8 $muted|Out-Null;text $s 510 801 38 18 '1 / 2' 8 $teal $true|Out-Null

# Page 2: objectives, design, status, and limitations.
$s=$doc.Slides.Add(2,12);$s.Background.Fill.ForeColor.RGB=rgb '#FFFFFF'
box $s 0 0 12 842 $teal|Out-Null
text $s 48 42 500 30 'PROJECT SUMMARY' 18 $dark $true|Out-Null
text $s 48 84 500 24 'Problem addressed' 12 $teal $true|Out-Null
text $s 48 112 500 62 'Pregnancy-related tasks and information can be difficult to manage across separate tools. MaatriCare offers a single place for users to maintain their own records and prepare for discussions with their healthcare professionals.' 10.5 $ink|Out-Null
text $s 48 187 500 24 'Objectives' 12 $teal $true|Out-Null
text $s 56 216 490 104 '1. Organize pregnancy profile, appointments, daily care tasks, wellbeing records, and milestones.\n2. Support private symptom journaling and user-controlled sharing of selected summaries.\n3. Provide general educational resources and tools for preparing healthcare questions.\n4. Offer configurable reminders, including opt-in scheduled browser push with timezone-aware delivery.' 10 $ink|Out-Null
text $s 48 333 500 24 'Method and system design' 12 $teal $true|Out-Null
text $s 48 361 500 132 'The frontend communicates with authenticated REST endpoints. The backend validates requests, checks user ownership, persists records through JPA repositories, and exposes profile, tracking, education, report, and reminder services. Scheduled Web Push uses a service worker and browser subscription keys; device credentials are stored separately from the user authentication token. Reminder payloads use generic text, and notification snoozes use expiring, one-time capabilities. Reminder scheduling defaults to Asia/Kolkata (Delhi time).' 10 $ink|Out-Null
text $s 48 505 500 24 'Current status and limitations' 12 $teal $true|Out-Null
text $s 48 533 500 118 'The application modules described are implemented in the project workspace. Focused tests cover timezone defaults and validation, reminder schedules and category preferences, deduplication, one-time snooze actions, and retention. The frontend production build passes. Live push delivery has not yet been demonstrated because deployment VAPID keys and HTTPS configuration are not supplied. This project is an organizational and educational aid, not a clinical diagnostic or treatment system.' 10 $ink|Out-Null
text $s 48 662 500 24 'Future work' 12 $teal $true|Out-Null
text $s 48 690 500 82 'Configure production VAPID keys and public HTTPS; validate live delivery on a real device; expand database and frontend testing; complete accessibility and privacy reviews; and prepare the final project report using the official institutional template.' 10 $ink|Out-Null
box $s 48 790 500 1 '#D7E3DE'|Out-Null;text $s 48 801 400 18 'Review 2 Abstract | MaatriCare' 8 $muted|Out-Null;text $s 510 801 38 18 '2 / 2' 8 $teal $true|Out-Null

$doc.SaveAs($editable,24)
$doc.SaveAs($out,32)
$doc.Close();$app.Quit()
Get-Item $editable,$out | Select-Object Name,Length,LastWriteTime
