$ErrorActionPreference = 'Stop'
$root = 'C:\project\MCA_Project\Review2_Submission'
New-Item -ItemType Directory -Force -Path $root | Out-Null
$outPpt = Join-Path $root 'R2_BATCH_MCA_9224116032_Deepak_Kumar.pptx'
$outAbstractDocx = Join-Path $root 'R2_BATCH_MCA_9224116032_Deepak_Kumar_Abstract.docx'
$outAbstractPdf = Join-Path $root 'R2_BATCH_MCA_9224116032_Deepak_Kumar_Abstract.pdf'
$outScript = Join-Path $root 'R2_5-to-7-minute_Presentation_Script.docx'
$outReport = Join-Path $root 'Final_Review_Report_Outline_60-to-80-pages.docx'

function C([string]$hex) {
    $h = $hex.TrimStart('#')
    $r = [Convert]::ToInt32($h.Substring(0,2),16)
    $g = [Convert]::ToInt32($h.Substring(2,2),16)
    $b = [Convert]::ToInt32($h.Substring(4,2),16)
    return ($r + ($g -shl 8) + ($b -shl 16))
}
function Add-Rect($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$fill,[int]$shapeType=1,[string]$line='') {
    $s = $slide.Shapes.AddShape($shapeType,$x,$y,$w,$h)
    $s.Fill.ForeColor.RGB = (C $fill)
    if ($line) { $s.Line.ForeColor.RGB = (C $line); $s.Line.Weight = 1 } else { $s.Line.Visible = 0 }
    return $s
}
function Add-Text($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$text,[double]$size=17,[string]$color='#203A3A',[bool]$bold=$false,[string]$font='Aptos',[int]$align=1) {
    $s = $slide.Shapes.AddTextbox(1,$x,$y,$w,$h)
    $s.TextFrame.WordWrap = -1
    $s.TextFrame.MarginLeft = 2; $s.TextFrame.MarginRight = 2; $s.TextFrame.MarginTop = 1; $s.TextFrame.MarginBottom = 1
    $s.TextFrame.AutoSize = 0
    $r = $s.TextFrame.TextRange
    $r.Text = $text.Replace('\n',[Environment]::NewLine)
    $r.Font.Name = $font; $r.Font.Size = $size; $r.Font.Bold = $bold; $r.Font.Color.RGB = (C $color)
    $r.ParagraphFormat.Alignment = $align
    return $s
}
function Add-Card($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$heading,[string]$body,[string]$accent='#357A70') {
    Add-Rect $slide $x $y $w $h '#FFFFFF' 5 '#DCE8E3' | Out-Null
    Add-Rect $slide ($x+14) ($y+15) 5 ($h-30) $accent 1 | Out-Null
    $headingSize = if ($h -lt 90) { 13 } else { 17 }
    $bodySize = if ($h -lt 90) { 9 } else { 11 }
    Add-Text $slide ($x+30) ($y+11) ($w-45) 23 $heading $headingSize '#174A46' $true | Out-Null
    Add-Text $slide ($x+30) ($y+36) ($w-48) ($h-41) $body $bodySize '#4C6260' $false | Out-Null
}
function New-ContentSlide($ppt,$title,$kicker='REVIEW 2') {
    $s = $ppt.Slides.Add($ppt.Slides.Count+1,12)
    $s.FollowMasterBackground = 0
    $s.Background.Fill.ForeColor.RGB = (C '#F4F7F4')
    Add-Rect $s 0 0 960 7 '#1D7067' 1 | Out-Null
    Add-Text $s 38 20 880 20 $kicker 9 '#63817B' $true | Out-Null
    Add-Text $s 38 45 880 47 $title 27 '#163E3A' $true | Out-Null
    Add-Rect $s 38 502 884 1 '#D6E2DE' 1 | Out-Null
    Add-Text $s 38 508 760 18 'MaatriCare | MCA Project | Illustrative academic presentation' 8 '#70837F' | Out-Null
    Add-Text $s 870 507 48 18 ("{0:D2}" -f $ppt.Slides.Count) 9 '#357A70' $true 'Aptos' 3 | Out-Null
    return $s
}
function Add-Pill($slide,$x,$y,$text,$fill='#E2F1EC',$color='#23675F') {
    Add-Rect $slide $x $y 138 26 $fill 5 | Out-Null
    Add-Text $slide ($x+8) ($y+4) 122 16 $text 9 $color $true 'Aptos' 2 | Out-Null
}
function Add-UiMockup($slide,[double]$x,[double]$y,[double]$w,[double]$h,[string]$title,[string]$kind) {
    Add-Rect $slide $x $y $w $h '#FFFFFF' 5 '#CBDAD5' | Out-Null
    Add-Rect $slide $x $y $w 30 '#EAF2EF' 5 | Out-Null
    Add-Text $slide ($x+12) ($y+7) ($w-24) 15 ('MaatriCare   /   ' + $title) 9 '#276A61' $true | Out-Null
    if ($kind -eq 'journey') {
        Add-Text $slide ($x+15) ($y+43) ($w-30) 22 'Pregnancy journey' 14 '#244641' $true | Out-Null
        Add-Text $slide ($x+15) ($y+74) ($w*.47) 25 'Week 32  |  Third trimester' 11 '#365F59' $true | Out-Null
        Add-Rect $slide ($x+15) ($y+111) ($w-30) 10 '#DCEBE5' 5 | Out-Null
        Add-Rect $slide ($x+15) ($y+111) (($w-30)*.8) 10 '#38867A' 5 | Out-Null
        Add-Text $slide ($x+15) ($y+132) ($w-30) 22 '80% complete   |   8 weeks remaining' 9 '#61736F' | Out-Null
        Add-Card $slide ($x+15) ($y+170) (($w-40)/2) 83 'Daily completion' 'Care, water, vitamins, activity' '#D68A62'
        Add-Card $slide ($x+25+($w-40)/2) ($y+170) (($w-40)/2) 83 'Weekly summary' 'Recorded progress this week' '#38867A'
    } elseif ($kind -eq 'care') {
        Add-Text $slide ($x+15) ($y+42) ($w-30) 24 'Your gentle day' 15 '#244641' $true | Out-Null
        Add-Text $slide ($x+15) ($y+68) ($w-30) 20 'Choose what feels realistic today.' 9 '#61736F' | Out-Null
        Add-Card $slide ($x+15) ($y+98) ($w-30) 60 'Capacity-aware plan' 'Low energy | Steady | Good energy' '#38867A'
        Add-Card $slide ($x+15) ($y+167) ($w-30) 66 'Care checklist' 'Manage and complete daily care tasks' '#D68A62'
    } elseif ($kind -eq 'appointments') {
        Add-Text $slide ($x+15) ($y+42) ($w-30) 22 'Appointment history' 14 '#244641' $true | Out-Null
        Add-Pill $slide ($x+15) ($y+72) 'Upcoming  |  Past  |  All' | Out-Null
        Add-Rect $slide ($x+15) ($y+108) ($w-30) 50 '#FAFCFB' 5 '#DCE8E3' | Out-Null
        Add-Text $slide ($x+26) ($y+117) ($w-50) 18 'Routine prenatal visit' 10 '#244641' $true | Out-Null
        Add-Text $slide ($x+26) ($y+137) ($w-50) 15 'Dr. Sample  |  Community Clinic' 8 '#61736F' | Out-Null
        Add-Text $slide ($x+15) ($y+174) ($w-30) 44 'Prepare questions  |  Calendar export  |  Visit brief' 10 '#35675F' $true | Out-Null
    } elseif ($kind -eq 'journal') {
        Add-Text $slide ($x+15) ($y+42) ($w-30) 22 'Private symptom journal' 14 '#244641' $true | Out-Null
        Add-Text $slide ($x+15) ($y+72) ($w-30) 22 'Search symptoms and notes  |  Filter severity and dates' 9 '#61736F' | Out-Null
        Add-Card $slide ($x+15) ($y+106) ($w-30) 55 'Tired  |  Mild' 'User-recorded note; not a diagnosis.' '#D68A62'
        Add-Text $slide ($x+15) ($y+172) ($w-30) 45 'Private to account. No diagnostic interpretation.' 9 '#61736F' | Out-Null
    } elseif ($kind -eq 'push') {
        Add-Text $slide ($x+15) ($y+42) ($w-30) 22 'Push delivery settings' 14 '#244641' $true | Out-Null
        Add-Text $slide ($x+15) ($y+74) ($w-30) 34 'Timezone: Asia/Kolkata (Delhi)' 10 '#365F59' $true | Out-Null
        Add-Card $slide ($x+15) ($y+116) ($w-30) 62 'Push opt-in' 'Device subscription and category preferences' '#38867A'
        Add-Text $slide ($x+15) ($y+185) ($w-30) 45 'Server VAPID keys required before live delivery.' 9 '#9B5B3C' $true | Out-Null
    } elseif ($kind -eq 'report') {
        Add-Text $slide ($x+15) ($y+42) ($w-30) 22 'Pregnancy summary' 14 '#244641' $true | Out-Null
        Add-Card $slide ($x+15) ($y+76) ($w-30) 64 'Tracking summary' 'Tasks | water | activity | sleep' '#38867A'
        Add-Card $slide ($x+15) ($y+151) ($w-30) 66 'Appointments' 'Recorded visits and selected notes' '#D68A62'
        Add-Text $slide ($x+15) ($y+226) ($w-30) 30 'Journal data is opt-in for reports.' 9 '#61736F' | Out-Null
    } elseif ($kind -eq 'visitbrief') {
        Add-Text $slide ($x+15) ($y+42) ($w-30) 22 'Visit brief' 14 '#244641' $true | Out-Null
        Add-Card $slide ($x+15) ($y+76) ($w-30) 68 'Questions to discuss' 'Select saved visit questions' '#38867A'
        Add-Card $slide ($x+15) ($y+155) ($w-30) 66 'Journal entries' 'Explicitly choose what to include' '#D68A62'
        Add-Text $slide ($x+15) ($y+230) ($w-30) 28 'Preview before printing or sharing.' 9 '#61736F' | Out-Null
    }
    Add-Text $slide ($x+10) ($y+$h-20) ($w-20) 13 'ILLUSTRATIVE UI MOCKUP - replace with live screenshot' 7 '#9B6E58' $true 'Aptos' 2 | Out-Null
}

$pptApp = New-Object -ComObject PowerPoint.Application
$pptApp.Visible = -1
$presentation = $pptApp.Presentations.Add()
$presentation.PageSetup.SlideSize = 16
$presentation.PageSetup.SlideWidth = 960
$presentation.PageSetup.SlideHeight = 540
$dark = '#173B38'; $teal = '#28796F'; $coral = '#D88968'; $paper = '#F4F7F4'; $ink = '#203A3A'; $muted = '#5C716D'

# 1 Welcome
$s = $presentation.Slides.Add(1,12); $s.FollowMasterBackground=0; $s.Background.Fill.ForeColor.RGB=(C $dark)
Add-Rect $s 0 0 14 540 $coral 1 | Out-Null
Add-Pill $s 54 42 'REVIEW 2 | MCA' '#2C514C' '#D8E9E1' | Out-Null
Add-Text $s 55 120 780 68 'Welcome to my project review' 33 '#FFFFFF' $true | Out-Null
Add-Text $s 58 210 700 38 'MaatriCare: Pregnancy Support and Care Coordination' 20 '#DCEAE5' $true | Out-Null
Add-Text $s 58 304 640 70 'Presented by  Deepak Kumar\nRegister No. 9224116032' 17 '#FFFFFF' $true | Out-Null
Add-Text $s 58 394 640 45 'Master of Computer Applications (MCA)\nSemester: [FILL]   |   Batch: [FILL]' 13 '#FFFFFF' | Out-Null
Add-Rect $s 735 82 170 112 '#FFFFFF' 5 '#92B5AA' | Out-Null
Add-Text $s 752 113 136 48 'COLLEGE LOGO\nPLACEHOLDER' 12 $dark $true 'Aptos' 2 | Out-Null
Add-Text $s 58 493 650 20 'Department of Computer Applications | Kalasalingam Academy of Research and Education (CDOE)' 9 '#DCEAE5' | Out-Null

# 2 Title
$s = New-ContentSlide $presentation 'Project Title' '01 | PROJECT IDENTIFICATION'
Add-Card $s 52 130 410 270 'MaatriCare' 'Pregnancy support and care-coordination web application.\n\nA private organizer for appointments, daily care, journals, guides, reminders, and healthcare conversations.' $teal | Out-Null
Add-Text $s 505 142 390 31 'Student: Deepak Kumar' 17 $ink $true | Out-Null
Add-Text $s 505 186 390 26 'Register No.: 9224116032' 15 $muted | Out-Null
Add-Text $s 505 222 390 26 'Department: Computer Applications' 15 $muted | Out-Null
Add-Text $s 505 269 390 48 'Faculty Guide: [FILL]\nSemester / Batch: [FILL]' 14 $ink | Out-Null
Add-Text $s 505 345 390 40 'College logo: insert official KARE/CDoE asset' 11 '#9B5B3C' $true | Out-Null

# 3 Problem statement
$s=New-ContentSlide $presentation 'Problem Statement & Abstract' '02 | NEED AND RESPONSE'
Add-Card $s 48 126 414 306 'The problem' 'Pregnancy-related care creates many small coordination tasks: appointments, preparation questions, daily routines, symptom notes, and educational information. These details can be difficult to keep together and recall during a clinical visit.' '#D88968' | Out-Null
Add-Card $s 492 126 414 306 'The response' 'MaatriCare provides one authenticated web application to organize those records, track user-entered routines, prepare visit briefs, and offer general educational resources. It is a support and organization tool, not a diagnostic or treatment system.' $teal | Out-Null
Add-Text $s 54 448 850 30 'Scope boundary: clinician remains the source for diagnosis, treatment, medication instructions, and urgent care.' 13 '#8B583F' $true | Out-Null

# 4 Introduction
$s=New-ContentSlide $presentation 'Introduction, Objectives & Scope' '03 | PROJECT OVERVIEW'
Add-Card $s 48 126 276 305 'Overview' 'A full-stack application with React/TypeScript frontend, Spring Boot REST API, relational storage, JWT authentication, and optional local AI support.' $teal | Out-Null
Add-Card $s 342 126 276 305 'Objectives' 'Keep pregnancy profile, appointments, care tasks, journals, milestones, reminders, and reports organized. Help users prepare for conversations with their healthcare team.' $coral | Out-Null
Add-Card $s 636 126 276 305 'Scope & limits' 'User-controlled tracking, reminders, educational guides, and report sharing. No diagnosis, prescriptions, treatment decisions, emergency triage, or automatic clinician sharing.' '#557B6B' | Out-Null

# 5 Architecture
$s=New-ContentSlide $presentation 'System Architecture' '04 | DESIGN'
Add-Card $s 50 148 235 204 'Browser client' 'React 19\nTypeScript 6\nVite 8\nLucide icons\nService worker for push' $teal | Out-Null
Add-Text $s 294 223 48 30 '->' 24 $coral $true 'Aptos' 2 | Out-Null
Add-Card $s 348 148 260 204 'Application API' 'Spring Boot 3.5.5\nJWT + Spring Security\nValidation\nJPA repositories\nScheduled reminder service' '#557B6B' | Out-Null
Add-Text $s 617 223 48 30 '->' 24 $coral $true 'Aptos' 2 | Out-Null
Add-Card $s 670 148 238 204 'Relational data' 'MySQL local profile\nPostgreSQL configuration\nFlyway migrations\nUser-owned records\nH2 integration tests' '#D88968' | Out-Null
Add-Text $s 100 385 755 40 'Push service worker <-> browser push service (VAPID) | optional Ollama local model' 13 $ink $true 'Aptos' 2 | Out-Null
Add-Text $s 120 432 710 30 'HTTPS required for deployed push; VAPID keys must be configured by the operator.' 10 '#8B583F' $true 'Aptos' 2 | Out-Null

# 6 DFD
$s=New-ContentSlide $presentation 'Data Flow Diagram (Level 0)' '05 | SYSTEM DESIGN'
Add-Card $s 46 203 170 126 'Pregnant user' 'Enters records\nReviews reminders\nChooses report data' '#D88968' | Out-Null
Add-Text $s 222 249 46 24 '->' 22 $teal $true 'Aptos' 2 | Out-Null
Add-Card $s 274 156 210 210 'MaatriCare UI' 'Profile\nDaily care\nAppointments\nJournal\nGuides / reports' $teal | Out-Null
Add-Text $s 490 249 46 24 '->' 22 $teal $true 'Aptos' 2 | Out-Null
Add-Card $s 542 156 210 210 'Spring API' 'JWT access\nValidation\nOwnership checks\nReminder scheduler\nReport services' '#557B6B' | Out-Null
Add-Text $s 748 249 28 24 '->' 18 $teal $true 'Aptos' 2 | Out-Null
Add-Card $s 782 203 136 126 'Database' 'Users\nProfiles\nTracking\nReminders' '#D88968' | Out-Null
Add-Text $s 260 398 665 50 'Optional outbound flow: scheduler -> encrypted Web Push -> subscribed browser' 12 $muted $true 'Aptos' 2 | Out-Null

# 7 Use case
$s=New-ContentSlide $presentation 'UML Use-Case View' '06 | SYSTEM DESIGN'
Add-Rect $s 238 120 650 326 '#FFFFFF' 5 '#AFC6BD' | Out-Null
Add-Text $s 258 130 610 24 'MaatriCare system boundary' 12 $teal $true | Out-Null
Add-Text $s 64 224 142 66 'USER\n(pregnant person)' 15 $ink $true 'Aptos' 2 | Out-Null
$uc = @(
    [pscustomobject]@{Text='Manage profile';X=270;Y=176},
    [pscustomobject]@{Text='Track care & wellbeing';X=500;Y=176},
    [pscustomobject]@{Text='Manage appointments';X=270;Y=250},
    [pscustomobject]@{Text='Record private journal';X=500;Y=250},
    [pscustomobject]@{Text='Prepare visit brief';X=270;Y=324},
    [pscustomobject]@{Text='View guides / reports';X=500;Y=324}
)
foreach($u in $uc){ Add-Rect $s $u.X $u.Y 230 50 '#E8F2EE' 9 '#8CB2A6' | Out-Null; Add-Text $s ($u.X+10) ($u.Y+14) 210 22 $u.Text 11 $ink $true 'Aptos' 2 | Out-Null }
Add-Text $s 728 378 146 48 'Optional: enable push\nand share a report link' 8 $muted | Out-Null
Add-Text $s 52 407 160 20 'Actor uses authenticated workflows.' 9 $muted | Out-Null

# 8 Class diagram
$s=New-ContentSlide $presentation 'UML Class View (Core Domain)' '07 | SYSTEM DESIGN'
$classes=@(
    [pscustomobject]@{Name='User';Fields='id: UUID\nemail\ndisplayName\ntimezoneId';X=52;Y=142;W=205},
    [pscustomobject]@{Name='PregnancyProfile';Fields='lastMenstrualPeriod\ndueDate\nhealthDetails';X=285;Y=142;W=205},
    [pscustomobject]@{Name='Appointment';Fields='startsAt\nprovider / clinic\nreminderMinutesBefore';X=518;Y=142;W=205},
    [pscustomobject]@{Name='CareTask';Fields='taskDate\ncompleted\nTaskDetail';X=751;Y=142;W=155},
    [pscustomobject]@{Name='SymptomEntry';Fields='symptom\nseverity\noccurredAt / notes';X=170;Y=314;W=210},
    [pscustomobject]@{Name='PushReminder';Fields='key / type\nscheduledAt\nstatus / attempts';X=405;Y=314;W=210},
    [pscustomobject]@{Name='AppointmentQuestion';Fields='question\nanswered / answer';X=640;Y=314;W=245}
)
foreach($c in $classes){Add-Rect $s $c.X $c.Y $c.W 116 '#FFFFFF' 1 '#8AAEA4'|Out-Null;Add-Rect $s $c.X $c.Y $c.W 29 '#DCECE6' 1 '#8AAEA4'|Out-Null;Add-Text $s ($c.X+7) ($c.Y+7) ($c.W-14) 18 $c.Name 11 '#174A46' $true 'Aptos' 2|Out-Null;Add-Text $s ($c.X+9) ($c.Y+36) ($c.W-18) 72 $c.Fields 9 $ink|Out-Null}
Add-Text $s 83 449 795 24 'Ownership: user 1-to-many appointments, tasks, symptoms, questions, subscriptions, and reminders.' 10 $muted $true 'Aptos' 2 | Out-Null

# 9 ER diagram
$s=New-ContentSlide $presentation 'ER Model (Simplified)' '08 | DATA DESIGN'
$tables=@(
    [pscustomobject]@{Name='users';Fields='id PK\nemail UNIQUE\ntimezone_id';X=45;Y=146;W=185},
    [pscustomobject]@{Name='pregnancy_profiles';Fields='id PK\nuser_id FK UNIQUE\nLMP, due_date';X=270;Y=146;W=205},
    [pscustomobject]@{Name='appointments';Fields='id PK\nuser_id FK\nstarts_at';X=510;Y=146;W=190},
    [pscustomobject]@{Name='care_tasks';Fields='id PK\nuser_id FK\ntask_detail_id FK';X=735;Y=146;W=190},
    [pscustomobject]@{Name='symptom_entries';Fields='id PK\nuser_id FK\nseverity, occurred_at';X=45;Y=314;W=185},
    [pscustomobject]@{Name='daily_wellbeing';Fields='id PK\nuser_id FK\nentry_date UNIQUE';X=270;Y=314;W=205},
    [pscustomobject]@{Name='push_subscriptions';Fields='id PK\nuser_id FK\nendpoint_hash';X=510;Y=314;W=190},
    [pscustomobject]@{Name='push_reminders';Fields='id PK\nuser_id FK\ndeduplication_key';X=735;Y=314;W=190}
)
foreach($t in $tables){Add-Rect $s $t.X $t.Y $t.W 105 '#FFFFFF' 5 '#AAC3B9'|Out-Null;Add-Text $s ($t.X+10) ($t.Y+9) ($t.W-20) 20 $t.Name 11 $teal $true|Out-Null;Add-Text $s ($t.X+10) ($t.Y+36) ($t.W-20) 63 $t.Fields 9 $ink|Out-Null}
Add-Text $s 110 444 740 22 'All sensitive records are user-owned; user deletion removes subscriptions and scheduled reminders.' 10 $muted $true 'Aptos' 2 | Out-Null

# 10 reminder flow
$s=New-ContentSlide $presentation 'Scheduled Reminder Flow' '09 | BACKGROUND DELIVERY'
$flow=@(
    [pscustomobject]@{Text='Preferences\nAsia/Kolkata';X=45},
    [pscustomobject]@{Text='Build occurrence\nfrom records';X=225},
    [pscustomobject]@{Text='Deduplicate\n& persist';X=405},
    [pscustomobject]@{Text='Due? Claim\natomically';X=585},
    [pscustomobject]@{Text='VAPID push\nto browser';X=765}
)
foreach($f in $flow){Add-Rect $s $f.X 190 150 82 '#FFFFFF' 5 '#AFC6BD'|Out-Null;Add-Text $s ($f.X+10) 211 130 45 $f.Text 11 $ink $true 'Aptos' 2|Out-Null}
foreach($x in @(195,375,555,735)){Add-Text $s ($x) 214 28 30 '->' 20 $coral $true 'Aptos' 2|Out-Null}
Add-Card $s 133 321 310 102 'Success' 'Mark sent; ignore duplicates on subsequent scheduler runs.' $teal | Out-Null
Add-Card $s 511 321 310 102 'Failure / snooze' 'Retry with backoff; snooze for one hour using an expiring, one-time capability.' $coral | Out-Null
Add-Text $s 190 442 560 19 'Generic lock-screen text; no symptoms or health details in push payload.' 10 $muted $true 'Aptos' 2 | Out-Null

# 11 requirements
$s=New-ContentSlide $presentation 'Hardware & Software Requirements' '10 | IMPLEMENTATION'
Add-Card $s 52 134 405 297 'Suggested development machine' 'Minimum: dual-core CPU, 8 GB RAM, 10 GB available storage.\n\nRecommended: quad-core CPU, 16 GB RAM, SSD, stable internet.\n\nThese are project setup recommendations, not benchmarked minimums.' $coral | Out-Null
Add-Card $s 498 134 410 297 'Software stack' 'Windows / Linux / macOS\nJava 25; Maven 3.9+\nNode.js 26; npm\nMySQL local profile; PostgreSQL config\nModern Chrome / Edge\nOptional Ollama local model\nVAPID keys + HTTPS for deployed push' $teal | Out-Null

# 12 modules
$s=New-ContentSlide $presentation 'Modules Developed' '11 | IMPLEMENTED FUNCTIONALITY'
$mods=@('Authentication & profile','Pregnancy status & milestones','Daily care & Gentle Day Planner','Appointments & visit questions','Private symptom journal','Weekly guides & resources','AI assistant / visit review','Reports & secure sharing','Emergency contacts','Timezone-aware reminders / Web Push')
for($i=0;$i -lt $mods.Count;$i++){ $col=$i%2; $row=[Math]::Floor($i/2); Add-Rect $s (52+$col*438) (128+$row*67) 410 52 '#FFFFFF' 5 '#D8E4DF'|Out-Null;Add-Rect $s (68+$col*438) (145+$row*67) 18 18 $teal 9|Out-Null;Add-Text $s (98+$col*438) (143+$row*67) 344 26 $mods[$i] 12 $ink $true|Out-Null }

# 13 screen panel
$s=New-ContentSlide $presentation 'Implemented Screen: Overview & Daily Care' '12 | MODULE WALKTHROUGH'
Add-UiMockup $s 54 126 405 330 'Pregnancy overview' 'journey'
Add-UiMockup $s 500 126 405 330 'Daily care' 'care'
Add-Text $s 77 466 805 18 'Illustrative UI mockups based on implemented screens; replace with live screenshots before submitting.' 9 '#9B5B3C' $true 'Aptos' 2 | Out-Null

# 14 appointment journal
$s=New-ContentSlide $presentation 'Implemented Screen: Appointments & Journal' '13 | MODULE WALKTHROUGH'
Add-UiMockup $s 54 126 405 330 'Appointments' 'appointments'
Add-UiMockup $s 500 126 405 330 'Journal' 'journal'
Add-Text $s 77 466 805 18 'Illustrative UI mockups based on implemented screens; replace with live screenshots before submitting.' 9 '#9B5B3C' $true 'Aptos' 2 | Out-Null

# 15 communication
$s=New-ContentSlide $presentation 'Implemented Screen: Resources, Reports & Reminders' '14 | MODULE WALKTHROUGH'
Add-UiMockup $s 52 135 275 288 'Reports' 'report'
Add-UiMockup $s 342 135 275 288 'Visit brief' 'visitbrief'
Add-UiMockup $s 632 135 275 288 'Push settings' 'push'
Add-Text $s 68 452 834 32 'Secure report links are time-limited and revocable. Push delivery remains unavailable until server VAPID keys are configured.' 10 '#8B583F' $true 'Aptos' 2 | Out-Null

# 16 sample I/O
$s=New-ContentSlide $presentation 'Sample Input & Output' '15 | DEMONSTRATION'
Add-Card $s 52 143 380 286 'Sample input' 'Appointment title: Routine prenatal visit\nStart: 23 Oct 2026, 10:00 IST\nReminder: 1 day before\n\nDaily care: 09:00 Asia/Kolkata\nPush: user opted in' $coral | Out-Null
Add-Text $s 446 245 70 34 '->' 26 $teal $true 'Aptos' 2 | Out-Null
Add-Card $s 530 143 378 286 'Sample output' 'Persisted occurrence: PENDING\nScheduled at: 22 Oct 2026, 10:00 IST\nDevice receives generic reminder\nTap: open reminder center\nSnooze: reschedule +1 hour' $teal | Out-Null
Add-Text $s 77 445 810 28 'Example uses synthetic data; actual delivery requires VAPID configuration and a subscribed device.' 10 '#8B583F' $true 'Aptos' 2 | Out-Null

# 17 sample code
$s=New-ContentSlide $presentation 'Sample Program Code: Reminder Dispatch' '16 | IMPLEMENTATION SAMPLE'
Add-Rect $s 58 132 844 294 '#183A38' 5 '#315B54' | Out-Null
$code = @'
@Scheduled(fixedDelayString = "${notifications.scheduler.interval-ms:30000}")
public void dispatchDueReminders() {
    if (!pushService.isConfigured()) return;
    for (UUID id : schedulingService.synchronizeAndFindDue()) {
        Delivery d = schedulingService.claim(id);
        if (d == null) continue;
        boolean sent = pushService.sendToUser(
            d.userId(), d.key(), d.title(), d.body(), d.snoozeToken());
        schedulingService.finish(id, sent);
    }
}
'@
Add-Text $s 82 160 795 230 $code 15 '#E8F2EE' $false 'Consolas' | Out-Null
Add-Text $s 83 440 794 26 'Server-side schedule, claim, deliver, then record success or retry state.' 10 $muted $true 'Aptos' 2 | Out-Null

# 18 sample dataset
$s=New-ContentSlide $presentation 'Sample Data Set (Synthetic)' '17 | DATA MODEL'
Add-Text $s 58 126 820 25 'Example only - no real account or health information' 11 '#9B5B3C' $true | Out-Null
$headers=@('Table','Sample row','Purpose');$xs=@(55,215,655);$ws=@(150,420,245)
for($i=0;$i -lt 3;$i++){Add-Rect $s $xs[$i] 163 $ws[$i] 31 '#DCECE6' 1 '#9AB8AE'|Out-Null;Add-Text $s ($xs[$i]+8) 170 ($ws[$i]-16) 17 $headers[$i] 10 $ink $true|Out-Null}
$rows=@(
    [pscustomobject]@{Table='users';Sample='demo-01 | timezone=Asia/Kolkata';Purpose='account settings'},
    [pscustomobject]@{Table='appointments';Sample='appt-01 | 23 Oct 2026 | 1440 min';Purpose='scheduled reminder source'},
    [pscustomobject]@{Table='daily_wellbeing';Sample='demo-01 | 18 Oct | 5 glasses';Purpose='daily tracking'},
    [pscustomobject]@{Table='symptom_entries';Sample='demo-01 | Tired | MILD';Purpose='private journal'},
    [pscustomobject]@{Table='push_reminders';Sample='key=appointment:... | PENDING';Purpose='deduped delivery state'},
    [pscustomobject]@{Table='push_subscriptions';Sample='endpoint_hash=sha256(...)';Purpose='per-device opt-in'}
)
foreach($row in $rows){ $y=199+([array]::IndexOf($rows,$row))*40;$cells=@($row.Table,$row.Sample,$row.Purpose);for($i=0;$i -lt 3;$i++){Add-Rect $s $xs[$i] $y $ws[$i] 37 '#FFFFFF' 1 '#D8E4DF'|Out-Null;Add-Text $s ($xs[$i]+8) ($y+9) ($ws[$i]-16) 20 $cells[$i] 9 $ink ($i -eq 0)|Out-Null}}

# 19 testing / conclusion
$s=New-ContentSlide $presentation 'Testing, Results & Next Steps' '18 | VALIDATION AND STATUS'
Add-Card $s 50 132 402 300 'Verified now' '4 focused push tests pass:\n- Asia/Kolkata default and validation\n- Local-time schedule + category preferences\n- One-time expiring snooze capability\n- 90-day terminal-record pruning\n\nFrontend production build passes.' $teal | Out-Null
Add-Card $s 494 132 414 300 'Conclusion & future work' 'Implemented modules support tracking, coordination, education, reports, and reminders.\n\nNext: configure VAPID + HTTPS, run real-device push test, replace mockup panels with screenshots, complete accessibility/security review, prepare KARE-format report.' $coral | Out-Null
Add-Text $s 75 451 810 22 'Live push delivery was not demonstrated because VAPID keys are not configured.' 10 '#9B5B3C' $true 'Aptos' 2 | Out-Null

# 20 refs / thanks
$s=New-ContentSlide $presentation 'References & Thank You' '19 | CLOSE'
Add-Card $s 53 128 500 304 'Suggested references - verify before use' 'Pressman & Maxim, Software Engineering (9th ed., 2019)\nSommerville, Software Engineering (10th ed., 2015)\nWHO, Recommendations on Antenatal Care (2016)\nReact documentation - react.dev\nSpring Boot / Security - docs.spring.io\nPostgreSQL - postgresql.org/docs\nMDN Service Worker and Push APIs - developer.mozilla.org\nKeep only sources actually consulted.' $teal | Out-Null
Add-Rect $s 590 138 310 270 $dark 5 | Out-Null
Add-Text $s 620 192 250 45 'Thank you' 30 '#FFFFFF' $true 'Aptos' 2 | Out-Null
Add-Text $s 622 253 245 81 'Thank you to my guide, department, college, and Review Committee.' 14 '#DCEAE5' $false 'Aptos' 2 | Out-Null
Add-Text $s 612 355 270 25 'Questions?' 15 '#F0B297' $true 'Aptos' 2 | Out-Null

$presentation.SaveAs($outPpt,24)
$presentation.Close()
$pptApp.Quit()
Write-Output 'Generated PowerPoint:'
Get-Item $outPpt | Select-Object Name,Length,LastWriteTime
return

# Two-page abstract in Word, with editable source and PDF export.
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Add()
$doc.PageSetup.TopMargin = 50; $doc.PageSetup.BottomMargin = 50; $doc.PageSetup.LeftMargin = 58; $doc.PageSetup.RightMargin = 58
$selection = $word.Selection
function W-Text($selection,[string]$text,[int]$size=11,[bool]$bold=$false,[string]$color='203A3A') {
    $selection.Font.Name = 'Aptos'; $selection.Font.Size = $size; $selection.Font.Bold = $bold
    $selection.Font.Color = [int]([Convert]::ToInt32($color.Substring(0,2),16) + ([Convert]::ToInt32($color.Substring(2,2),16) -shl 8) + ([Convert]::ToInt32($color.Substring(4,2),16) -shl 16))
    $selection.TypeText($text.Replace('\n',[Environment]::NewLine))
}
W-Text $selection 'MAATRICARE' 22 $true '174A46'; $selection.TypeParagraph()
W-Text $selection 'Pregnancy Support and Care-Coordination Application' 15 $true; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Student: Deepak Kumar    |    Register No.: 9224116032' 11 $true; $selection.TypeParagraph()
W-Text $selection 'Department: Computer Applications    |    Guide: [FILL]' 10 $false; $selection.TypeParagraph()
W-Text $selection 'Semester: [FILL]    |    Batch: [FILL]    |    College: Kalasalingam Academy of Research and Education (CDOE)' 10 $false; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'ABSTRACT' 14 $true '28796F'; $selection.TypeParagraph()
W-Text $selection 'Pregnancy involves ongoing appointments, daily routines, personal records, and conversations with healthcare professionals. When these details are scattered across calendars, notes, and memory, users may find it difficult to organize tasks or prepare questions for a visit. MaatriCare is a web-based pregnancy support and care-coordination application designed to bring these organizational activities together in one authenticated workspace.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'The application provides pregnancy profile and progress information, appointment management, visit-question preparation, daily care tracking, a private symptom journal, pregnancy milestones, educational guides and resources, healthcare summaries, emergency contacts, and reminder preferences. Users can select recorded information for reports and control whether a report is shared through a time-limited link. The interface supports recording and reviewing information; it does not diagnose medical conditions, recommend treatment, or replace professional or emergency care.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'MaatriCare is implemented as a React and TypeScript frontend using Vite, with a Java Spring Boot REST backend. The backend uses Spring Security and JWT-based authentication, Jakarta validation, Spring Data JPA, and relational database support for MySQL in local development and PostgreSQL configuration. User-owned data includes pregnancy profiles, appointments, tasks, wellbeing records, symptom entries, milestones, questions and answers, report shares, emergency contacts, and scheduled push reminders. Flyway migrations define the production-oriented schema; local development uses the configured MySQL profile.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Implemented reminder functionality includes in-app reminders and optional scheduled Web Push. The backend reconciles configured reminder categories, stores reminder occurrences to avoid duplicates, applies retry handling, and uses Asia/Kolkata as the current default timezone. Push registration is opt-in and can be disabled per account. Live push delivery requires operator-provided VAPID keys and a secure browser context; the project’s focused tests validate timezone behavior, category preferences, deduplication, snooze capability, and retention.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'The system is intended as an organizational and educational aid for users and their care teams. Its safety boundary is explicit: user-entered records are not clinical diagnoses, and generated summaries or educational content must be reviewed with a healthcare professional when relevant. Future work includes configuring and validating live push on HTTPS, adding broader frontend and database integration tests, accessibility and privacy review, completing deployment setup, and preparing the final report using the official institutional template.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Keywords: pregnancy support, care coordination, appointment management, symptom journal, Web Push, Spring Boot, React' 10 $true '28796F'
$selection.InsertBreak(7)
W-Text $selection 'PROJECT SUMMARY' 14 $true '28796F'; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Problem addressed' 12 $true; $selection.TypeParagraph()
W-Text $selection 'Pregnancy-related information and responsibilities can be difficult to keep organized. MaatriCare provides a single place for users to maintain their own records and prepare for discussions with their healthcare professionals.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Objectives' 12 $true; $selection.TypeParagraph()
W-Text $selection '1. Organize pregnancy profile, appointments, daily care tasks, wellbeing records, and milestones.\n2. Support private symptom journaling and user-controlled sharing of selected summary information.\n3. Provide general educational resources and tools for preparing healthcare questions.\n4. Offer configurable reminders, including opt-in scheduled browser push, with timezone-aware delivery.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Method and system design' 12 $true; $selection.TypeParagraph()
W-Text $selection 'The frontend communicates with authenticated REST endpoints. The backend validates requests, applies user-ownership checks, persists records through JPA repositories, and exposes profile, tracking, education, report, and reminder services. Scheduled Web Push uses a service worker and browser subscription keys; per-device credentials are stored separately from the user’s authentication token. Reminder payloads use generic text, and snooze actions use expiring one-time capabilities. The implementation was verified with focused H2-backed backend tests and a TypeScript/Vite production build.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Current status and limitations' 12 $true; $selection.TypeParagraph()
W-Text $selection 'The application modules described above are implemented in the project workspace. Automated verification covers selected backend behaviors and compilation; it is not a substitute for a full external security, accessibility, or clinical review. Actual Web Push delivery has not been demonstrated because deployment VAPID keys and HTTPS configuration are not yet supplied. Educational and AI features are organizational aids and must not be used for diagnosis, medication changes, treatment decisions, or emergency triage.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Future work' 12 $true; $selection.TypeParagraph()
W-Text $selection 'Configure a production VAPID key pair and public HTTPS frontend; conduct a real-device push test; expand frontend, migration, and database integration tests; complete accessibility and privacy reviews; and prepare the final 60–80 page report in the official KARE format.' 11; $selection.TypeParagraph(); $selection.TypeParagraph()
W-Text $selection 'Student: Deepak Kumar  |  Register No.: 9224116032' 10 $true
$doc.SaveAs2($outAbstractDocx,16)
$doc.ExportAsFixedFormat($outAbstractPdf,17)
$doc.Close($false); $word.Quit()

# Speaker script and demo checklist in Word.
$word = New-Object -ComObject Word.Application; $word.Visible=$false; $scriptDoc=$word.Documents.Add(); $scriptSel=$word.Selection
W-Text $scriptSel 'REVIEW 2 | 5-7 MINUTE PRESENTATION SCRIPT' 18 $true '174A46'; $scriptSel.TypeParagraph()
W-Text $scriptSel 'Target duration: about 6 minutes. Speak naturally, keep the camera on, and demonstrate only synthetic/demo data.' 10; $scriptSel.TypeParagraph(); $scriptSel.TypeParagraph()
$scriptParts=@(
@('0:00-0:20 | Slides 1-2','Good morning. I am Deepak Kumar, Register Number 9224116032, from the Department of Computer Applications. My project is MaatriCare, a pregnancy support and care-coordination web application. My semester, batch, and guide details are shown as placeholders and will be completed before submission.'),
@('0:20-0:50 | Slides 3-4','The problem is that appointments, daily care routines, symptom notes, and questions for healthcare professionals can be spread across different places. MaatriCare brings those organization features into one authenticated workspace. The goal is to support record keeping and communication, not to diagnose or replace medical care.'),
@('0:50-1:30 | Slides 5-10','The frontend is built with React and TypeScript. It communicates with a Spring Boot API protected by Spring Security and JWT. JPA repositories store user-owned records in a relational database. The design includes use cases for profile management, daily tracking, appointments, journaling, guides, and reports. The reminder flow reconciles preferences in Asia/Kolkata time, stores occurrences to prevent duplicates, and sends push only when configured.'),
@('1:30-1:55 | Slides 11-12','The development stack uses Java 25, Maven, Node, React, Spring Boot, and MySQL locally, with PostgreSQL configuration and H2-backed tests. Major modules include account and pregnancy profile, daily care, appointments, journal, guides, reports, emergency contacts, and notifications.'),
@('1:55-3:20 | Slides 13-15 and live demo','I will now show the application using synthetic demo data. On Overview, the user sees pregnancy progress and a summary of recorded activities. In Daily Care, the user can set a manageable focus list and track hydration, vitamins, activity, and sleep. In Appointments, the user can create or review a visit and prepare questions. The Journal supports private notes, date and severity filtering, and a safety reminder. Guides and reports provide educational and organizational support. These mockup panels are illustrative and should be replaced by clean live screenshots in the submitted deck.'),
@('3:20-3:55 | Slides 16-18','For a sample reminder, a user selects a visit and a reminder interval. The backend stores a scheduled occurrence using the configured timezone. At delivery, a subscribed browser receives generic reminder text. The sample code shows the scheduled dispatch and retry path. The sample records in the data slide are synthetic and do not represent a real user.'),
@('3:55-4:35 | Slide 19','Focused tests cover the Asia/Kolkata default and validation, schedule and category preferences, reminder deduplication, one-time snooze capabilities, and cleanup of terminal records after 90 days. The frontend production build also passes. Live push delivery has not been demonstrated yet because VAPID keys and HTTPS deployment configuration are still required.'),
@('4:35-5:00 | Slide 20','In conclusion, MaatriCare provides a connected organizer for pregnancy-related records and conversations. Future work includes live push deployment, broader integration and accessibility tests, a privacy review, and completion of the institutional final report. Thank you. I welcome your questions.')
)
foreach($part in $scriptParts){W-Text $scriptSel $part[0] 11 $true '28796F';$scriptSel.TypeParagraph();W-Text $scriptSel $part[1] 10;$scriptSel.TypeParagraph();$scriptSel.TypeParagraph()}
$scriptSel.InsertBreak(7);W-Text $scriptSel 'RECORDING CHECKLIST' 14 $true '174A46';$scriptSel.TypeParagraph()
W-Text $scriptSel '1. Replace [FILL] semester, batch, guide name, and college-logo placeholder.\n2. Replace all illustrative UI mockups with clean live screenshots from the app.\n3. Keep camera on, test the microphone, and record the screen with narration for 5-7 minutes.\n4. Do not show real health data; use the disposable demo account or synthetic values.\n5. Save the final video as R2_[Batch]_MCA_9224116032_Deepak_Kumar.mp4.\n6. Before recording push behavior, set VAPID keys and confirm HTTPS or localhost secure context.\n7. Review the exported PDF for exactly two pages before LMS upload.' 10
$scriptDoc.SaveAs2($outScript,16);$scriptDoc.Close($false);$word.Quit()

# Final report outline with page budget; final must match official KARE template.
$word = New-Object -ComObject Word.Application; $word.Visible=$false; $reportDoc=$word.Documents.Add();$reportSel=$word.Selection
W-Text $reportSel 'FINAL PROJECT REPORT OUTLINE' 18 $true '174A46';$reportSel.TypeParagraph()
W-Text $reportSel 'Target: 60-80 pages. This is a planning outline, not a final report. Apply the official KARE/CDOE template and supervisor requirements before submission.' 10; $reportSel.TypeParagraph();$reportSel.TypeParagraph()
$outline=@(
'Front matter (4-6 pages): cover, certificate, declaration, acknowledgement, abstract, contents, figures and tables.',
'Chapter 1 - Introduction (5-7 pages): background, problem statement, objectives, scope, users, limitations, report organization.',
'Chapter 2 - Literature and existing systems (6-8 pages): comparable pregnancy organizers, notification systems, privacy considerations, gap analysis. Do not make clinical efficacy claims.',
'Chapter 3 - Requirements and feasibility (6-8 pages): functional/non-functional requirements, use cases, user roles, hardware/software requirements, safety and privacy boundaries.',
'Chapter 4 - System analysis and design (10-14 pages): architecture, DFD, use-case/class diagrams, ER model, API flow, authentication/ownership, reminder lifecycle.',
'Chapter 5 - Implementation (12-16 pages): React UI, Spring REST API, schema/migrations, authentication, care tracking, appointments, journal, guides, reports, Web Push setup.',
'Chapter 6 - Testing and results (8-10 pages): test environment, selected backend tests, build results, screenshots, sample inputs/outputs, limitations and untested deployment cases.',
'Chapter 7 - Conclusion and future work (3-4 pages): completed scope, live VAPID/HTTPS prerequisite, integration/accessibility/privacy work.',
'References (2-3 pages): official framework/database documentation, relevant books/papers actually consulted, access dates.',
'Appendices (5-8 pages): selected source code, API examples, schema/migration excerpts, test output, user guide.'
)
foreach($item in $outline){W-Text $reportSel $item 11 $false;$reportSel.TypeParagraph();$reportSel.TypeParagraph()}
W-Text $reportSel 'Suggested total: approximately 65-75 pages, excluding any institution-required front matter. Confirm the exact format, declaration, certificates, and page-count rules with the guide.' 10 $true '9B5B3C'
$reportDoc.SaveAs2($outReport,16);$reportDoc.Close($false);$word.Quit()

Write-Output 'Generated:'
Get-Item $outPpt,$outAbstractDocx,$outAbstractPdf,$outScript,$outReport | Select-Object Name,Length
