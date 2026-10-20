$ErrorActionPreference = 'Stop'
$root = 'C:\project\MCA_Project\Review2_Submission'
$sample = Join-Path $root 'R2-MCA_Project_Jan24-91241160033-RAM.pptx'
$work = Join-Path $root ("R2_BATCH_MCA_9224116032_Deepak_Kumar_SampleTheme_WORK_{0}.pptx" -f [guid]::NewGuid().ToString('N'))
$final = Join-Path $root 'R2_BATCH_MCA_9224116032_Deepak_Kumar_SampleTemplate_Final.pptx'
Copy-Item -LiteralPath $sample -Destination $work -Force

function Set-ShapeText($shape,[string]$value,[double]$size=18,[string]$font='Times New Roman',[bool]$bold=$false,[int]$alignment=1,[double]$width=0,[double]$height=0) {
    $shape.TextFrame.WordWrap=-1
    $shape.TextFrame.AutoSize=0
    $shape.TextFrame.TextRange.Text=$value.Replace('\n',[Environment]::NewLine).Replace('`n',[Environment]::NewLine)
    $shape.TextFrame.TextRange.Font.Name=$font
    $shape.TextFrame.TextRange.Font.Size=$size
    $shape.TextFrame.TextRange.Font.Bold=$bold
    $shape.TextFrame.TextRange.ParagraphFormat.Alignment=$alignment
    if($width -gt 0){$shape.Width=$width}
    if($height -gt 0){$shape.Height=$height}
}
function Replace-ShapeText($slide,[int]$index,[string]$value,[double]$size=18,[string]$font='Times New Roman',[bool]$bold=$true,[int]$alignment=1,[double]$width=0,[double]$height=0,[string]$color='#00008B') {
    $old=$slide.Shapes.Item($index)
    $x=$old.Left;$y=$old.Top;$oldWidth=$old.Width;$oldHeight=$old.Height
    $old.Delete()
    if($width -gt 0){$oldWidth=$width};if($height -gt 0){$oldHeight=$height}
    return (Add-Text $slide $x $y $oldWidth $oldHeight $value $size $color $bold 'Times New Roman' $alignment)
}
function Add-Text($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$value,[double]$size=12,[string]$color='#1A1A1A',[bool]$bold=$false,[string]$font='Times New Roman',[int]$alignment=1) {
    $shape=$slide.Shapes.AddTextbox(1,$x,$y,$w,$h)
    $shape.TextFrame.WordWrap=-1;$shape.TextFrame.AutoSize=0
    $shape.TextFrame.MarginLeft=2;$shape.TextFrame.MarginRight=2;$shape.TextFrame.MarginTop=1;$shape.TextFrame.MarginBottom=1
    $shape.TextFrame.TextRange.Text=$value.Replace('\n',[Environment]::NewLine).Replace('`n',[Environment]::NewLine)
    $shape.TextFrame.TextRange.Font.Name=$font;$shape.TextFrame.TextRange.Font.Size=$size;$shape.TextFrame.TextRange.Font.Bold=$bold
    $shape.TextFrame.TextRange.ParagraphFormat.Alignment=$alignment
    $shape.TextFrame.TextRange.Font.Color.RGB=Color $color
    return $shape
}
function Color([string]$hex){$h=$hex.TrimStart('#');return ([Convert]::ToInt32($h.Substring(0,2),16)+([Convert]::ToInt32($h.Substring(2,2),16)-shl 8)+([Convert]::ToInt32($h.Substring(4,2),16)-shl 16))}
function Add-Box($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$fill='#FFFFFF',[string]$line='#274D92',[int]$kind=1){$shape=$slide.Shapes.AddShape($kind,$x,$y,$w,$h);$shape.Fill.ForeColor.RGB=Color $fill;$shape.Line.ForeColor.RGB=Color $line;$shape.Line.Weight=1;return $shape}
function Add-Entity($slide,[double]$x,[double]$y,[double]$w,[string]$name,[string]$fields){
    Add-Box $slide $x $y $w 48 '#FFFFFF' '#3564A2' 1|Out-Null
    Add-Box $slide $x $y $w 17 '#E8F0FA' '#3564A2' 1|Out-Null
    Add-Text $slide ($x+4) ($y+1) ($w-8) 14 $name 8 '#173E79' $true|Out-Null
    Add-Text $slide ($x+4) ($y+19) ($w-8) 26 $fields 7 '#222222' $false|Out-Null
}
function Add-Arrow($slide,[double]$x1,[double]$y1,[double]$x2,[double]$y2){$line=$slide.Shapes.AddConnector(1,$x1,$y1,$x2,$y2);$line.Line.ForeColor.RGB=Color '#365F99';$line.Line.Weight=1.1;$line.Line.EndArrowheadStyle=2;return $line}
function Add-UiMock($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$caption,[string]$kind){
    Add-Box $slide $x $y $w $h '#F7F9FC' '#3A64A0' 1|Out-Null
    Add-Box $slide ($x+8) ($y+8) ($w-16) 22 '#174A89' '#174A89' 1|Out-Null
    Add-Text $slide ($x+15) ($y+10) ($w-30) 17 ('MaatriCare  |  '+$caption) 9 '#FFFFFF' $true 'Times New Roman' 1|Out-Null
    if($kind -eq 'dashboard'){
        Add-Text $slide ($x+16) ($y+39) ($w-32) 23 'Pregnancy journey  |  Week 32' 11 '#173E79' $true|Out-Null
        Add-Box $slide ($x+16) ($y+68) ($w-32) 10 '#DCE6F3' '#DCE6F3' 1|Out-Null
        Add-Box $slide ($x+16) ($y+68) (($w-32)*0.8) 10 '#3168A8' '#3168A8' 1|Out-Null
        Add-Text $slide ($x+16) ($y+84) ($w-32) 18 'Daily completion  |  Weekly care summary' 8 '#333333' $false|Out-Null
        Add-Box $slide ($x+16) ($y+107) (($w-38)/2) ($h-130) '#FFFFFF' '#CCD8E6' 1|Out-Null
        Add-Text $slide ($x+23) ($y+113) (($w-52)/2) ($h-144) 'CARE TASKS\nHydration\nVitamins\nMovement\nSleep' 8 '#222222' $false|Out-Null
        Add-Box $slide ($x+22+($w-38)/2) ($y+107) (($w-38)/2) ($h-130) '#FFFFFF' '#CCD8E6' 1|Out-Null
        Add-Text $slide ($x+29+($w-38)/2) ($y+113) (($w-52)/2) ($h-144) 'TODAY\n5 / 8 glasses\nVitamin recorded\n15 min activity' 8 '#222222' $false|Out-Null
    } elseif($kind -eq 'appointments'){
        Add-Text $slide ($x+16) ($y+40) ($w-32) 18 'Upcoming  |  Past  |  All visits' 8 '#173E79' $true|Out-Null
        Add-Box $slide ($x+16) ($y+64) ($w-32) 43 '#FFFFFF' '#CCD8E6' 1|Out-Null
        Add-Text $slide ($x+23) ($y+68) ($w-46) 34 'Routine prenatal visit\nDr. Sample  |  Community Clinic' 8 '#222222' $false|Out-Null
        Add-Text $slide ($x+16) ($y+116) ($w-32) 32 'Prepare questions  |  Visit brief  |  Calendar export' 8 '#173E79' $true|Out-Null
    } elseif($kind -eq 'journal'){
        Add-Text $slide ($x+16) ($y+40) ($w-32) 18 'Private symptom journal' 10 '#173E79' $true|Out-Null
        Add-Text $slide ($x+16) ($y+63) ($w-32) 18 'Search  |  Date range  |  Severity' 8 '#333333' $false|Out-Null
        Add-Box $slide ($x+16) ($y+88) ($w-32) 42 '#FFFFFF' '#CCD8E6' 1|Out-Null
        Add-Text $slide ($x+22) ($y+93) ($w-44) 34 'Tired  |  Mild\nRecorded note for clinician discussion' 8 '#222222' $false|Out-Null
    } elseif($kind -eq 'guides'){
        Add-Text $slide ($x+16) ($y+40) ($w-32) 20 'Guides and resources' 10 '#173E79' $true|Out-Null
        Add-Text $slide ($x+16) ($y+65) ($w-32) 47 'Weekly guide\nSearchable education library\nHealthcare summary and sharing controls' 8 '#222222' $false|Out-Null
    }
    Add-Text $slide ($x+6) ($y+$h-14) ($w-12) 12 'Illustrative interface based on implemented modules' 6 '#555555' $false 'Times New Roman' 2|Out-Null
}

$app=New-Object -ComObject PowerPoint.Application
$app.Visible=-1
$pres=$app.Presentations.Open($work,$false,$false,$false)
# Remove repeated testing/module slides while preserving the sample's exact slide master/theme.
foreach($index in @(23,21,19,15,12)){$pres.Slides.Item($index).Delete()}

# Cover retains sample's official image/logo, date, semester, MCA and CDOE band.
$slide=$pres.Slides.Item(1)
Set-ShapeText $slide.Shapes.Item(1) 'Jan -2024' 26 'Times New Roman' $true 2
Set-ShapeText $slide.Shapes.Item(4) 'Project Work Review - 2' 24 'Times New Roman' $true 2
Set-ShapeText $slide.Shapes.Item(5) 'Centre for Distance and Online Education' 18 'Times New Roman' $true 2

# Student/title page uses the original sample's field layout.
$slide=$pres.Slides.Item(2)
Set-ShapeText $slide.Shapes.Item(1) "Name of the Learner : Deepak Kumar`nRegister number : 9224116032`nName of the Programme : MCA`nSemester : IV`nName of the Course : Project Work`nBatch : Jan 2024`nCourse Code : 223MCA4201`nRegulation : 2022`nFaculty Advisor : Mr. M. GOVINDARAJ" 18 'Times New Roman' $false 1
Set-ShapeText $slide.Shapes.Item(2) 'Project Title : MaatriCare' 22 'Times New Roman' $true 1

# Abstract slide follows the sample text layout.
$slide=$pres.Slides.Item(3)
Set-ShapeText $slide.Shapes.Item(1) 'ABSTRACT' 22 'Times New Roman' $true 2
Set-ShapeText $slide.Shapes.Item(2) 'MaatriCare is a pregnancy support and care-coordination web application that organizes pregnancy profiles, appointments, daily care, wellbeing records, symptom journals, milestones, learning resources, reports, and reminders in one authenticated system. Users can prepare questions for healthcare visits, create a printable visit brief, and choose which records to include in a report.\n\nThe frontend is implemented with React, TypeScript, and Vite. The backend uses Java Spring Boot REST APIs, Spring Security with JWT authentication, request validation, and Spring Data JPA. MySQL supports local development, with PostgreSQL configured for production. Relational data includes user profiles, appointments, care tasks, wellbeing entries, symptoms, milestones, emergency contacts, report shares, push subscriptions, and scheduled reminders.\n\nReminder scheduling defaults to Asia/Kolkata time. Optional Web Push uses server-side deduplication, retries, and one-hour snooze actions. Live push requires VAPID credentials and HTTPS. Focused backend tests validate timezone defaults, reminder preferences, deduplication, snooze capabilities, and retention; the frontend production build passes.\n\nMaatriCare is an organizational and educational aid. It does not diagnose conditions, recommend treatment, or replace a healthcare professional or emergency services.' 18 'Times New Roman' $false 1

# Introduction and scope.
$slide=$pres.Slides.Item(4)
Set-ShapeText $slide.Shapes.Item(2) 'Introduction' 22 'Times New Roman' $true 1
Set-ShapeText $slide.Shapes.Item(1) 'Project Overview\nMaatriCare is a private workspace for pregnancy-related organization and care coordination.\n\nObjectives\n- Keep pregnancy profile and progress information together.\n- Track daily care and wellbeing entries.\n- Manage appointments, questions, and visit briefs.\n- Provide educational resources, summaries, and configurable reminders.\n\nScope and Safety\nThe system supports user-entered records and healthcare conversations. It does not diagnose, prescribe, or provide emergency triage.' 15 'Times New Roman' $false 1

# System architecture and DFD on the sample scope slide.
$slide=$pres.Slides.Item(5)
$slide.Shapes.Item(1).Delete()
Set-ShapeText $slide.Shapes.Item(1) 'System Design: Architecture and DFD' 18 'Times New Roman' $true 1 560 36
Add-Text $slide 20 70 122 48 'USER\nProfile / tracking' 12 '#173E79' $true|Out-Null
Add-Text $slide 160 70 150 48 'REACT FRONTEND\nForms / dashboard' 12 '#173E79' $true|Out-Null
Add-Text $slide 330 70 160 48 'SPRING BOOT API\nJWT / validation' 12 '#173E79' $true|Out-Null
Add-Text $slide 510 70 170 48 'RELATIONAL DATA\nMySQL / PostgreSQL' 12 '#173E79' $true|Out-Null
Add-Arrow $slide 142 95 160 95|Out-Null;Add-Arrow $slide 310 95 330 95|Out-Null;Add-Arrow $slide 490 95 510 95|Out-Null
Add-Text $slide 63 158 240 84 'Modules\nDaily Care | Appointments\nJournal | Guides | Reports' 13 '#222222' $false|Out-Null
Add-Text $slide 344 158 300 84 'Services\nUser ownership checks | JPA repositories\nScheduled reminder processor' 13 '#222222' $false|Out-Null
Add-Text $slide 115 278 500 54 'Optional outbound flow: Scheduler -> encrypted Web Push -> subscribed browser' 13 '#173E79' $true 'Times New Roman' 2|Out-Null
Add-Text $slide 75 340 590 34 'Local push delivery requires VAPID credentials; deployed service workers require HTTPS.' 11 '#333333' $false 'Times New Roman' 2|Out-Null

# ER diagram using sample diagram slide/background.
$slide=$pres.Slides.Item(6)
Set-ShapeText $slide.Shapes.Item(1) 'ER (Entity Relationship) Diagram' 18 'Times New Roman' $true 1 670 36
$slide.Shapes.Item(2).Delete()
$entities=@(
    [pscustomobject]@{N='users';F='id PK | email UNIQUE | timezone';X=250;Y=55;W=220},
    [pscustomobject]@{N='pregnancy_profiles';F='id PK | user_id FK UNIQUE';X=25;Y=119;W=210},
    [pscustomobject]@{N='appointments';F='id PK | user_id FK | starts_at';X=255;Y=119;W=210},
    [pscustomobject]@{N='care_tasks';F='id PK | user_id FK | task_detail_id FK';X=485;Y=119;W=210},
    [pscustomobject]@{N='daily_wellbeing';F='user_id FK | entry_date UNIQUE';X=25;Y=180;W=210},
    [pscustomobject]@{N='symptom_entries';F='id PK | user_id FK | severity';X=255;Y=180;W=210},
    [pscustomobject]@{N='pregnancy_milestones';F='id PK | user_id FK | week_number';X=485;Y=180;W=210},
    [pscustomobject]@{N='emergency_contacts';F='id PK | user_id FK | type';X=25;Y=241;W=210},
    [pscustomobject]@{N='report_shares';F='id PK | user_id FK | expires_at';X=255;Y=241;W=210},
    [pscustomobject]@{N='push_subscriptions';F='id PK | user_id FK | endpoint_hash';X=485;Y=241;W=210},
    [pscustomobject]@{N='push_reminders';F='id PK | user_id FK | scheduled_at';X=25;Y=302;W=210},
    [pscustomobject]@{N='appointment_questions';F='id PK | appointment_id FK';X=255;Y=302;W=210},
    [pscustomobject]@{N='task_details';F='id PK | title | is_shared';X=485;Y=302;W=210}
)
# User-owned records connect to users; appointment questions connect to appointments and care tasks to shared task details.
Add-Arrow $slide 360 103 360 112|Out-Null
Add-Arrow $slide 130 112 590 112|Out-Null
foreach($cx in @(130,360,590)){
    Add-Arrow $slide $cx 112 $cx 119|Out-Null
    Add-Arrow $slide $cx 165 $cx 180|Out-Null
    Add-Arrow $slide $cx 226 $cx 241|Out-Null
    Add-Arrow $slide $cx 287 $cx 302|Out-Null
}
foreach($entity in $entities){Add-Box $slide $entity.X $entity.Y $entity.W 46 '#FFFFFF' '#3564A2' 1|Out-Null;Add-Box $slide $entity.X $entity.Y $entity.W 17 '#DDE8F3' '#3564A2' 1|Out-Null;Add-Text $slide ($entity.X+4) ($entity.Y+1) ($entity.W-8) 14 $entity.N 8 '#173E79' $true|Out-Null;Add-Text $slide ($entity.X+4) ($entity.Y+19) ($entity.W-8) 24 $entity.F 7 '#222222' $false|Out-Null}
Add-Text $slide 40 354 640 30 'Relationships: users 1:N to user-owned records; pregnancy_profiles 1:1. appointment_questions reference appointments; care_tasks reference task_details.' 8 '#333333' $false 'Times New Roman' 2|Out-Null

# Use-case plus compact class view on the sample use-case slide.
$slide=$pres.Slides.Item(7)
Set-ShapeText $slide.Shapes.Item(1) 'UML Use-Case and Class Diagrams' 18 'Times New Roman' $true 1 520 36
$slide.Shapes.Item(2).Delete()
Add-Text $slide 28 62 295 19 'USE CASES' 11 '#173E79' $true 'Times New Roman' 2|Out-Null
Add-Text $slide 28 92 75 36 'USER' 10 '#222222' $true 'Times New Roman' 2|Out-Null
$useCases=@('Manage profile','Track daily care','Manage appointments','Journal and review','Prepare reports')
for($i=0;$i -lt $useCases.Count;$i++){ $y=74+($i*54);$oval=$slide.Shapes.AddShape(9,124,$y,170,36);$oval.Fill.ForeColor.RGB=Color '#FFFFFF';$oval.Line.ForeColor.RGB=Color '#3564A2';Add-Text $slide 130 ($y+7) 158 20 $useCases[$i] 8 '#222222' $false 'Times New Roman' 2|Out-Null;Add-Arrow $slide 100 ($y+18) 124 ($y+18)|Out-Null }
Add-Text $slide 354 62 330 19 'CORE CLASSES' 11 '#173E79' $true 'Times New Roman' 2|Out-Null
$classRows=@(
    [pscustomobject]@{N='User';F='id | email | timezone';Y=91},
    [pscustomobject]@{N='Appointment';F='user_id | starts_at | reminder';Y=151},
    [pscustomobject]@{N='CareTask';F='user_id | date | completed';Y=211},
    [pscustomobject]@{N='SymptomEntry';F='user_id | symptom | severity';Y=271},
    [pscustomobject]@{N='PushReminder';F='user_id | scheduled_at | status';Y=331}
)
foreach($row in $classRows){Add-Box $slide 354 $row.Y 320 43 '#FFFFFF' '#3564A2' 1|Out-Null;Add-Text $slide 361 ($row.Y+5) 306 16 $row.N 9 '#173E79' $true|Out-Null;Add-Text $slide 361 ($row.Y+22) 306 16 $row.F 7 '#222222' $false|Out-Null}

# Requirements.
$slide=$pres.Slides.Item(8)
Set-ShapeText $slide.Shapes.Item(1) 'Hardware and Software Requirements' 18 'Times New Roman' $true 1 650 36
Set-ShapeText $slide.Shapes.Item(2) 'HARDWARE (DEVELOPMENT RECOMMENDATION)\nPROCESSOR: Dual-core or better\nRAM: 8 GB minimum; 16 GB recommended\nSTORAGE: 10 GB available\nDISPLAY: 1366 x 768 or greater\nNETWORK: Internet for package installation and backend testing' 13 'Times New Roman' $false 1
Set-ShapeText $slide.Shapes.Item(3) 'SOFTWARE REQUIREMENTS\nOPERATING SYSTEM: Windows 10/11 or Linux\nFRONT END: React, TypeScript, Vite\nBACK END: Java 25, Spring Boot 3.5.5, Maven\nDATABASE: MySQL local; PostgreSQL configuration; H2 tests\nTOOLS: Node.js/npm, Git, modern browser\nOPTIONAL: Ollama; VAPID configuration for Web Push' 14 'Times New Roman' $false 1

# Modules list.
$slide=$pres.Slides.Item(9)
Set-ShapeText $slide.Shapes.Item(1) 'Module Name' 18 'Times New Roman' $true 1 500 36
Set-ShapeText $slide.Shapes.Item(2) 'Implemented modules include:\n1) Authentication and pregnancy profile\n2) Pregnancy progress and milestones\n3) Daily care and Gentle Day Planner\n4) Appointment and visit-question management\n5) Private symptom journal\n6) Guides and resource library\n7) Reports and secure sharing\n8) Emergency contacts\n9) Reminder preferences and Web Push' 14 'Times New Roman' $false 1

# Daily care UI mockup.
$slide=$pres.Slides.Item(10)
Set-ShapeText $slide.Shapes.Item(1) 'Daily Care and Gentle Day Planner' 17 'Times New Roman' $true 1 650 36
Set-ShapeText $slide.Shapes.Item(2) 'The daily care module tracks user-selected routines, including hydration, vitamins, activity, sleep, and mood. The Gentle Day Planner lets a user choose an energy level and a manageable focus list. Completion updates are saved to the account.\n\nThis module supports organization only; it does not prescribe a care plan.' 13 'Times New Roman' $false 1
$slide.Shapes.Item(3).Delete()
Add-UiMock $slide 118 193 495 178 'Daily Care' 'dashboard'

# Appointment module.
$slide=$pres.Slides.Item(11)
Set-ShapeText $slide.Shapes.Item(1) 'Appointment and Visit Brief Module' 17 'Times New Roman' $true 1 650 36
$slide.Shapes.Item(4).Delete();$slide.Shapes.Item(2).Delete()
Set-ShapeText $slide.Shapes.Item(2) 'Users can create, search, and review upcoming or past appointments, record provider details, prepare questions, export calendar events, and generate a printable visit brief. The brief includes only information the user explicitly selects.' 13 'Times New Roman' $false 1
Add-UiMock $slide 143 199 460 168 'Appointments' 'appointments'

# Journal and education modules.
$slide=$pres.Slides.Item(12)
Set-ShapeText $slide.Shapes.Item(1) 'Private Journal and Learning Modules' 16 'Times New Roman' $true 1 650 36
Set-ShapeText $slide.Shapes.Item(2) 'Journal: record symptoms, severity, dates, and private notes; search and filter your entries.\n\nGuides: review general, week-based information and searchable educational resources. Discuss personal questions with a healthcare professional.' 11 'Times New Roman' $false 1 696 120
Add-UiMock $slide 155 203 445 155 'Journal and Guides' 'journal'

# Sample input/output.
$slide=$pres.Slides.Item(13)
Set-ShapeText $slide.Shapes.Item(1) 'Sample Input and Output' 18 'Times New Roman' $true 1 620 36
$slide.Shapes.Item(2).Delete()
Add-Box $slide 34 75 315 260 '#FFFFFF' '#5476A4' 1|Out-Null
Add-Box $slide 370 75 315 260 '#FFFFFF' '#5476A4' 1|Out-Null
Add-Text $slide 50 91 283 24 'SAMPLE INPUT' 12 '#173E79' $true 'Times New Roman' 2|Out-Null
Add-Text $slide 50 123 283 190 'Appointment: Routine prenatal visit\nStart: 23 Oct 2026, 10:00 IST\nReminder: 1 day before\nTimezone: Asia/Kolkata\n\nData is synthetic.' 12 '#222222' $false|Out-Null
Add-Text $slide 386 91 283 24 'SAMPLE OUTPUT' 12 '#173E79' $true 'Times New Roman' 2|Out-Null
Add-Text $slide 386 123 283 190 'Reminder: PENDING\nScheduled: 22 Oct 2026, 10:00 IST\nNotification: generic reminder\nAction: open app or snooze\nSnooze: +1 hour' 12 '#222222' $false|Out-Null

# UI code sample.
$slide=$pres.Slides.Item(14)
Set-ShapeText $slide.Shapes.Item(1) 'Sample Code : UI : TypeScript : Appointment History' 14 'Times New Roman' $true 1 670 36
$slide.Shapes.Item(2).Delete()
$uiCode=@'
const visibleVisits = appointments
  .filter((visit) => matchesView(visit, view))
  .filter((visit) => matchesSearch(visit, query))
  .sort(compareByDate);

return visibleVisits.map((visit) => (
  <button key={visit.id}
    aria-pressed={visit.id === selectedId}
    onClick={() => selectVisit(visit.id)}>
    <strong>{visit.title}</strong>
    <time>{formatVisitDate(visit.startsAt)}</time>
  </button>
));
'@
Add-Box $slide 29 73 660 302 '#F8FAFC' '#A9BED9' 1|Out-Null
Add-Text $slide 44 89 630 268 $uiCode 12 '#202020' $false 'Consolas' 1|Out-Null

# Backend program sample.
$slide=$pres.Slides.Item(15)
Set-ShapeText $slide.Shapes.Item(1) 'Sample Code : Backend : Spring Boot : Reminder Scheduler' 14 'Times New Roman' $true 1 680 36
for($i=$slide.Shapes.Count;$i -ge 2;$i--){$slide.Shapes.Item($i).Delete()}
$backendCode=@'
@Scheduled(fixedDelayString = "${notifications.scheduler.interval-ms:30000}")
public void dispatchDueReminders() {
    if (!pushService.isConfigured()) return;
    for (UUID id : scheduler.synchronizeAndFindDue()) {
        Delivery d = scheduler.claim(id);
        if (d == null) continue;
        boolean sent = push.sendToUser(
            d.userId(), d.key(), d.title(), d.body(), d.snoozeToken());
        scheduler.finish(id, sent);
    }
}
'@
Add-Box $slide 23 70 665 310 '#F8FAFC' '#A9BED9' 1|Out-Null
Add-Text $slide 41 85 635 276 $backendCode 12 '#202020' $false 'Consolas' 1|Out-Null

# Synthetic dataset.
$slide=$pres.Slides.Item(16)
Set-ShapeText $slide.Shapes.Item(1) 'Sample Data Set : MySQL / PostgreSQL' 16 'Times New Roman' $true 1 580 36
$slide.Shapes.Item(2).Delete()
$headers=@('TABLE','SAMPLE FIELDS','PURPOSE');$x=@(22,192,505);$w=@(160,305,190)
for($c=0;$c -lt 3;$c++){Add-Box $slide $x[$c] 69 $w[$c] 30 '#DDE8F5' '#5277A8' 1|Out-Null;Add-Text $slide ($x[$c]+7) 75 ($w[$c]-14) 18 $headers[$c] 10 '#1A3761' $true|Out-Null}
$rows=@(
    @('users','id, email, timezone_id','account'),
    @('appointments','user_id, starts_at, reminder_minutes_before','visit schedule'),
    @('care_tasks','user_id, task_date, completed','daily tasks'),
    @('symptom_entries','user_id, symptom, severity','private journal'),
    @('push_reminders','user_id, key, scheduled_at, status','scheduled delivery'),
    @('push_subscriptions','user_id, endpoint_hash','opt-in device')
)
for($r=0;$r -lt $rows.Count;$r++){for($c=0;$c -lt 3;$c++){ $yy=100+($r*43);Add-Box $slide $x[$c] $yy $w[$c] 40 '#FFFFFF' '#C8D3E1' 1|Out-Null;Add-Text $slide ($x[$c]+7) ($yy+8) ($w[$c]-14) 25 $rows[$r][$c] 9 '#222222' ($c -eq 0)|Out-Null}}
Add-Text $slide 30 367 660 20 'All example records are synthetic and contain no real health information.' 9 '#555555' $false 'Times New Roman' 2|Out-Null

# Testing methods and results.
$slide=$pres.Slides.Item(17)
Set-ShapeText $slide.Shapes.Item(1) 'Testing Methods and Results' 18 'Times New Roman' $true 1 610 36
Set-ShapeText $slide.Shapes.Item(2) 'Backend: Spring Boot Test, MockMvc, JUnit, and H2 PostgreSQL mode. Four focused reminder tests passed.\n\nCoverage: timezone default and validation; schedule preferences; deduplication; one-time expiring snooze; 90-day retention.\n\nFrontend: TypeScript/Vite production build passed.\n\nPrivacy checks: JWT authentication, ownership checks, selective report sharing, generic push text.\n\nLimitations: no live push delivery without VAPID keys and HTTPS; broader UI, accessibility, and MySQL integration tests remain future work.' 13 'Times New Roman' $false 1

# Conclusion and future work.
$slide=$pres.Slides.Item(18)
Set-ShapeText $slide.Shapes.Item(1) 'Conclusion and Future Work' 18 'Times New Roman' $true 1 620 36
Set-ShapeText $slide.Shapes.Item(3) 'MaatriCare provides an authenticated workspace for pregnancy records, appointments, daily care, private journaling, educational resources, user-controlled reports, and reminders. The system focuses on organization and communication support.\n\nFuture work\n- Configure VAPID and HTTPS, then test push on real devices\n- Replace illustrative interface panels with live screenshots\n- Extend frontend, database, migration, and accessibility tests\n- Complete privacy/security review and production deployment\n- Prepare the final report in the official institutional format' 13 'Times New Roman' $false 1 606 298

# References slide from sample, followed by its original guide/thank-you slide.
$slide=$pres.Slides.Item(19)
Set-ShapeText $slide.Shapes.Item(1) 'Reference Books and Websites' 18 'Times New Roman' $true 1 600 36
Set-ShapeText $slide.Shapes.Item(2) 'React Documentation. React.dev.\nSpring Boot Reference Documentation. Spring.io.\nSpring Security Reference Documentation. Spring.io.\nPostgreSQL Documentation. PostgreSQL.org.\nMySQL 8.0 Reference Manual. Oracle.\nMDN Web Docs: Service Worker API and Push API.\nPressman and Maxim. Software Engineering: A Practitioner’s Approach, 9th ed.\nSommerville. Software Engineering, 10th ed.\nVerify publication details and retain only sources actually consulted.' 13 'Times New Roman' $false 1
$slide=$pres.Slides.Item(20)
Set-ShapeText $slide.Shapes.Item(1) 'I sincerely thank my project guide: Mr. M. GOVINDARAJ Sir.' 17 'Times New Roman' $true 2 606 64
$slide.Shapes.Item(1).Left=55;$slide.Shapes.Item(1).Top=105
Set-ShapeText $slide.Shapes.Item(2) 'Thank You' 28 'Arial' $true 2 420 65
$slide.Shapes.Item(2).Left=150;$slide.Shapes.Item(2).Top=230
$thanksExtra=Add-Text $slide 70 315 580 32 'The CDOE and Review Committee' 15 '#138A4B' $true 'Times New Roman' 2

Write-Output "Pre-save slide 1 title: $($pres.Slides.Item(1).Shapes.Item(4).TextFrame.TextRange.Text)"
Write-Output "Pre-save slide 3 title: $($pres.Slides.Item(3).Shapes.Item(1).TextFrame.TextRange.Text)"
Write-Output "Pre-save slide 5 title: $($pres.Slides.Item(5).Shapes.Item(1).TextFrame.TextRange.Text)"
$pres.SaveAs($final,24)
Write-Output "Final styled deck: $final"
Write-Output "Slide count: $($pres.Slides.Count)"
$pres.Close()
$app.Quit()
