import numpy as np
import cv2
from PIL import Image

def reduce_colors(img, num_colors=16):
    # 画像を指定した色数に制限
    return img.convert('P', palette=Image.ADAPTIVE, colors=num_colors)

def create_pixel_art(input_img, output_path, pixel_size=32, num_colors=16):
    # 画像をPILのImageオブジェクトに変換
    img = Image.fromarray(input_img)

    # 画像のリサイズ（ピクセルアート風にするために小さくする）
    small_img = img.resize((pixel_size, pixel_size), resample=Image.BOX)

    # 再度、元のサイズにリサイズする
    pixel_art_img = small_img.resize(img.size, Image.BOX)

    # 色数を制限したピクセルアートを作成
    reduced_color_img = reduce_colors(pixel_art_img, num_colors)

    # 結果を保存
    reduced_color_img.save(output_path)
    print(f'Pixel art saved to {output_path}')

    # 保存した画像のパスを返す
    return output_path

def process_image(input_image_path, output_image_path, pixel_size=32, num_colors=16):
    # カラー画像の読み込み（グレースケール指定）
    gray_org_img = cv2.imread(input_image_path, 0)

    # Cannyアルゴリズム実行
    canny_edges = cv2.Canny(gray_org_img, 100, 200)

    # エッジ画像の二値化（完全に黒と白に分ける）
    _, binary_edges = cv2.threshold(canny_edges, 127, 255, cv2.THRESH_BINARY)

    # 明示的に黒（255未満）をすべて0に統一
    binary_edges[binary_edges < 255] = 0

    # 二値化後の画像を反転（背景が黒、線が白になるようにする）
    inverted_edges = cv2.bitwise_not(binary_edges)

    # 二値化後の画像をPILのImageオブジェクトに変換
    inverted_edges_pil = Image.fromarray(inverted_edges)

    # ピクセルアートを作成し、結果を保存
    return create_pixel_art(np.array(inverted_edges_pil), output_image_path, pixel_size, num_colors)
