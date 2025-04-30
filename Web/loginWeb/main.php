<?php
session_start();
if (!isset($_SESSION['username'])) {
    header("Location: login.php");
    exit();
}

include_once "koneksi.php";
$conn = koneksidb();

$username = $_SESSION['username'];

$sql = "SELECT * FROM pengguna WHERE username = $1";
pg_prepare($conn, "get_user", $sql);
$result = pg_execute($conn, "get_user", array($username));

if ($row = pg_fetch_assoc($result)) {
    $namaLengkap = $row['name'];
    $usernameFix = $row['username'];
    $bio = $row['bio'];
    $fotoProfil = $row['foto_profil'] ?? 'images/pp2.jpg';
    $id_pengguna = $row['id'];

    $sql_post = "SELECT id, content, created_at FROM post WHERE id_pengguna = $1 ORDER BY id DESC";
    $result_post = pg_query_params($conn, $sql_post, array($id_pengguna));

    if (!$result_post) {
        die("Error mengambil post: " . pg_last_error($conn));
    }

    $posts = [];
    while ($row = pg_fetch_assoc($result_post)) {
        $tanggal = 'Tanggal tidak diketahui';
        if (!empty($row['created_at'])) {
            try {
                $tanggal = date('F j, Y', strtotime($row['created_at']));
            } catch (Exception $e) {
                error_log("Error parsing date: " . $e->getMessage());
            }
        }

        $posts[] = [
            'id' => $row['id'],
            'name' => $namaLengkap,
            'username' => $usernameFix,
            'date' => $tanggal,
            'content' => $row['content'],
            'profilePic' => $fotoProfil,
        ];
    }
} else {
    echo "Pengguna tidak ditemukan.";
    exit();
}

$menuItems = [
    ['icon' => '🏠', 'text' => 'Home'],
    ['icon' => '🔍', 'text' => 'Explore'],
    ['icon' => '🔔', 'text' => 'Notifications'],
    ['icon' => '✉', 'text' => 'Messages'],
    ['icon' => '📊', 'text' => 'Grok'],
    ['icon' => '👥', 'text' => 'Communities'],
    ['icon' => '👤', 'text' => 'Profile'],
    ['icon' => '⋮', 'text' => 'More'],
];

$profile = [
    'name' => $namaLengkap,
    'username' => $usernameFix,
    'bio' => $bio,
    'details' => 'Joined ' . date('F Y', strtotime($row['created_at'] ?? 'September 2020')),
    'following' => $row['following_count'] ?? 12,
    'followers' => $row['followers_count'] ?? 4869,
    'posts' => count($posts),
    'cover' => $row['cover_photo'] ?? 'images/hdr.jpg',
    'profilePic' => $fotoProfil,
];

$tabs = ['Posts', 'Replies', 'Highlights', 'Articles', 'Media', 'Likes'];
$activeTab = 'Posts';

$sql_post = "SELECT id, content, created_at FROM post WHERE id_pengguna = $1 ORDER BY id DESC";
pg_prepare($conn, "get_posts", $sql_post);
$result_post = pg_execute($conn, "get_posts", array($id_pengguna));

if (!$result_post) {
    die("Error mengambil post: " . pg_last_error($conn));
}

$posts = [];
while ($row = pg_fetch_assoc($result_post)) {
    $tanggal = 'Tanggal tidak diketahui';
    
    if (!empty($row['created_at'])) {
        try {
            $tanggal = date('F j, Y', strtotime($row['created_at']));
        } catch (Exception $e) {
            error_log("Error parsing date: " . $e->getMessage());
        }
    }

    $posts[] = [
        'id' => $row['id'] ?? null,
        'name' => $namaLengkap,
        'username' => $usernameFix,
        'date' => $tanggal,
        'content' => $row['content'] ?? '',
        'profilePic' => $fotoProfil,
    ];
}

$suggestedUsers = [
    ['name' => 'ngaji dulu', 'username' => 'bismillah', 'image' => 'images/pp7.jpeg', 'following' => true],
    ['name' => 'ini nama', 'username' => 'sayabingung', 'image' => 'images/pp3.jpg', 'following' => false],
    ['name' => 'pusing banget', 'username' => 'imnotmatcha', 'image' => 'images/pp4.jpg', 'following' => false],
];

$trendingTopics = [
    ['category' => 'Trending in Indonesia', 'topic' => 'mie ayam', 'posts' => '1M'],
    ['category' => 'Music • Trending', 'topic' => 'aespa', 'posts' => '369K'],
    ['category' => 'Trending', 'topic' => '#hazza', 'posts' => '7,233'],
    ['category' => 'Trending in Indonesia', 'topic' => 'imut', 'posts' => '19.7K'],
];

$followingList = [
    ['name' => 'aemeowz', 'username' => 'aemeowz', 'image' => 'images/pp8.jpeg', 'following' => true, 'verified' => false, 'bio' => 'gatau'],
    ['name' => 'eve', 'username' => '_cultuurstelsel', 'image' => 'images/pp2.jpg', 'following' => true, 'verified' => false, 'bio' => 'halo'],
    ['name' => 'fastoberi', 'username' => 'fastoberi', 'image' => 'images/pp3.jpg', 'following' => true, 'verified' => false, 'bio' => 'selective.'],
    ['name' => 'joli', 'username' => 'gulaiayamkfc', 'image' => 'images/pp4.jpg', 'following' => true, 'verified' => false, 'bio' => 'sudah masuk fk unair ges'],
    ['name' => 'Kiara', 'username' => 'aenovaaa', 'image' => 'images/pp5.jpeg', 'following' => true, 'verified' => false, 'bio' => 'I\'m too spicy'],
    ['name' => 'g', 'username' => 'aesjayoon', 'image' => 'images/pp6.jpeg', 'following' => true, 'verified' => false, 'bio' => 'Cats gon\' copy one idea, two, then the whole look, then deny it'],
    ['name' => 'S', 'username' => 'yizuhos', 'image' => 'images/pp7.jpeg', 'following' => true, 'verified' => false, 'bio' => 'rubie'],
];

$showFollowingList = isset($_GET['show_following']);
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Twitter Profile UI</title>
    <link rel="stylesheet" href="main.css">
</head>
<body>
    <div class="container">
        <nav class="sidebar">
            <div class="sidebar-logo">
                <img src="images/logo.jpg" alt="Logo">
            </div>
            <ul>
                <?php foreach ($menuItems as $item): ?>
                    <li>
                        <span class="icon"><?= $item['icon'] ?></span> <span><?= $item['text'] ?></span>
                    </li>
                <?php endforeach; ?>
            </ul>
            <button class="post-btn" id="postingButton">Post</button>
        </nav>

        <div class="main-content">
            <?php if ($showFollowingList): ?>
                <div class="following-header">
                    <div class="back-btn">&larr;</div>
                    <h2><?= $profile['name'] ?></h2>
                </div>
                
                <div class="following-stats">
                    <div class="following-stat">
                        <a href="#">Verified Followers</a>
                    </div>
                    <div class="following-stat">
                        <a href="#">Followers</a>
                    </div>
                    <div class="following-stat active">
                        <a href="#">Following</a>
                    </div>
                </div>
                
                <div class="following-list">
                    <?php foreach ($followingList as $user): ?>
                        <div class="following-item">
                            <img src="<?= $user['image'] ?>" alt="<?= $user['name'] ?>">
                            <div class="following-info">
                                <div>
                                    <strong><?= $user['name'] ?></strong>
                                    <?php if ($user['verified'] ?? false): ?>
                                        <span class="verified-badge">✓</span>
                                    <?php endif; ?>
                                </div>
                                <p>@<?= $user['username'] ?></p>
                                <?php if (!empty($user['bio'])): ?>
                                    <p class="following-bio"><?= $user['bio'] ?></p>
                                <?php endif; ?>
                            </div>
                            <div class="following-btn-container">
                                <button class="<?= $user['following'] ? 'following-btn' : 'follow-btn' ?>">
                                    <?= $user['following'] ? 'Following' : 'Follow' ?>
                                </button>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            <?php else: ?>
                <div class="header">
                    <div class="profile-header">
                        <span class="back-btn">&larr;</span>
                        <div class="profile-name">
                            <strong><?= $profile['name'] ?></strong>
                            <p class="post-count"><?= $profile['posts'] ?> posts</p>
                        </div>
                    </div>
                    <img src="<?= $profile['cover'] ?>" class="cover" alt="Cover Image">
                    <img src="<?= $profile['profilePic'] ?>" class="profile-pic" alt="Profile Picture">
                </div>
                <div class="profile-info">
                    <div class="profile-details">
                        <h2><?= $profile['name'] ?></h2>
                        <p class="username">@<?= $profile['username'] ?></p>
                        <p class="bio"><?= $profile['bio'] ?></p>
                        <p class="details"><?= $profile['details'] ?></p>
                        <p class="stats">
                            <a href="?show_following=true"><strong><?= $profile['following'] ?></strong> Following</a> • 
                            <strong><?= $profile['followers'] ?></strong> Followers
                        </p>
                    </div>
                    <button class="edit-profile">Edit Profile</button>
                </div>
                <div class="tabs">
                    <?php foreach ($tabs as $tab): ?>
                        <span class="<?= ($activeTab === $tab) ? 'active' : '' ?>">
                            <?= $tab ?>
                        </span>
                    <?php endforeach; ?>
                </div>
                <div class="posts">
                    <?php foreach ($posts as $post): ?>
                        <div class="post">
                            <img src="<?= $post['profilePic'] ?>" class="post-pic">
                            <div class="post-content">
                                <p><strong><?= $post['name'] ?></strong> <span class="username">@<?= $post['username'] ?></span> • <span class="post-date"><?= $post['date'] ?></span></p>
                                <p><?= $post['content'] ?></p>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            <?php endif; ?>
        </div>

        <aside class="right-sidebar">
            <div class="search-bar">
                <input type="text" placeholder="Search">
            </div>

            <div class="suggestions">
                <h3>You might like</h3>
                <?php foreach ($suggestedUsers as $user): ?>
                    <div class="suggestion">
                        <img src="<?= $user['image'] ?>" class="suggestion-pic" alt="<?= $user['name'] ?>">
                        <div class="suggestion-info">
                            <strong><?= $user['name'] ?></strong>
                            <p>@<?= $user['username'] ?></p>
                        </div>
                        <button class="<?= $user['following'] ? 'following-btn' : 'follow-btn' ?>">
                            <?= $user['following'] ? 'Following' : 'Follow' ?>
                        </button>
                    </div>
                <?php endforeach; ?>
                <a href="#" class="show-more">Show more</a>
            </div>
            <div class="trending">
                <h3>What's happening</h3>
                <?php foreach ($trendingTopics as $trend): ?>
                    <div class="trending-item">
                        <small><?= $trend['category'] ?></small>
                        <p><?= $trend['topic'] ?></p>
                        <span><?= $trend['posts'] ?> posts</span>
                    </div>
                <?php endforeach; ?>
            </div>
        </aside>
    </div>

    <div id="unfollowModal" class="modal-overlay" style="display: none;">
        <div class="modal-content">
            <p class="modal-title">Unfollow <span id="targetUsername">@username</span>?</p>
            <p class="modal-desc">
                Their posts will no longer show up in your For You timeline. 
                You can still view their profile, unless their posts are protected.
            </p>
            <div class="modal-actions">
                <button class="unfollow-confirm">Unfollow</button>
                <button class="cancel-btn" onclick="closeModal()">Cancel</button>
            </div>
        </div>
    </div>


    <script>
        document.querySelectorAll('.following-btn').forEach(button => {
            button.addEventListener('click', () => {
                const parent = button.closest('.suggestion, .following-item');
                const username = parent.querySelector('.suggestion-info p, .following-info p').innerText;
                document.getElementById('targetUsername').innerText = username;
                document.getElementById('unfollowModal').style.display = 'flex';
            });
        });

        function closeModal() {
            document.getElementById('unfollowModal').style.display = 'none';
        }

        document.querySelector('.unfollow-confirm').addEventListener('click', () => {
            document.getElementById('unfollowModal').style.display = 'none';
            const toast = document.getElementById('unfollowNotice');
            toast.style.display = 'block';
            setTimeout(() => {
                toast.style.display = 'none';
            }, 3000);
        });

        document.querySelector('.back-btn').addEventListener('click', () => {
            window.history.back();
        });
    </script>

    <div id="unfollowNotice">You have unfollowed this user</div>
    
    <div id="postingModal" class="posting-modal-overlay">
        <div class="posting-modal-content">
            <div class="posting-modal-header">
                <button class="close-btn" id="closeModal">&times;</button>
            </div>
            <div class="posting-modal-body">
                <textarea class="post-input" placeholder="Apa yang sedang terjadi?" id="postContent"></textarea>
            </div>
            <div class="posting-modal-footer">
                <button class="post-submit" id="submitPost" disabled>Posting</button>
            </div>
        </div>
    </div>
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        const postingButton = document.getElementById('postingButton');
        const postingModal = document.getElementById('postingModal');
        const closeModal = document.getElementById('closeModal');
        const postContent = document.getElementById('postContent');
        const submitPost = document.getElementById('submitPost');

        postingButton.addEventListener('click', function() {
            postingModal.style.display = 'flex';
            postContent.focus();
        });

        closeModal.addEventListener('click', function() {
            postingModal.style.display = 'none';
        });

        postingModal.addEventListener('click', function(e) {
            if (e.target === postingModal) {
                postingModal.style.display = 'none';
            }
        });

        postContent.addEventListener('input', function() {
            submitPost.disabled = postContent.value.trim() === '';
        });

        submitPost.addEventListener('click', function() {
            const content = postContent.value.trim();

            const data = new FormData();
            data.append('content', content);

            fetch('create_post.php', {
                method: 'POST',
                body: data,
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Postingan berhasil dibuat!');
                    postContent.value = '';
                    postingModal.style.display = 'none';
                    submitPost.disabled = true;
                    window.location.reload();
                } else {
                    alert('Gagal: ' + result.error);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Terjadi kesalahan saat membuat postingan.');
            });
        });
    });
    </script>
</body>
</html>